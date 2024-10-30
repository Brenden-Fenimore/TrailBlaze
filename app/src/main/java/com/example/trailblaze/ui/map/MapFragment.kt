package com.example.trailblaze.ui.Map

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.example.trailblaze.BuildConfig.PLACES_API_KEY
import com.example.trailblaze.R
import com.example.trailblaze.databinding.FragmentMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.*
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.example.trailblaze.firestore.UserManager
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.kotlin.circularBounds
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.SearchNearbyRequest


class MapFragment : Fragment(),
    OnCameraMoveStartedListener,
OnCameraMoveListener,
OnCameraMoveCanceledListener,
OnCameraIdleListener,
OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    //Define a variable to hold the Places API key.
    val apiKey = PLACES_API_KEY

    private lateinit var map: GoogleMap
    private val fullSail : CameraPosition = CameraPosition.builder().target(LatLng(28.596472,-81.301472)).zoom(15f).build()
    var autocompletelist : List<AutocompletePrediction> = mutableListOf()
    var autocompletelistString : MutableList<String> = mutableListOf()
    val searchTypes = listOf("hiking_area", "park")
    lateinit var mapFragment : SupportMapFragment
    val currentUser = UserManager.getCurrentUser()




    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val root: View = binding.root


        // Get the SupportMapFragment and request notification when the map is ready to be used.
        mapFragment = this.childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //Set interactive buttons to a value to work with
        val multiAutoCompleteTextView = _binding!!.mapSearch
        val clearButton = _binding!!.clearMapsearchtext
        val fullsailButton = _binding!!.fullsail
        val satelliteButton = _binding!!.satellite
        val roadButton = _binding!!.roadmap
        val terrainButton = _binding!!.terrain
        val nearbySearchButton = _binding!!.nearbysearch
        val satelliteImageCircle = _binding!!.satelliteImage
        val roadImageCircle = _binding!!.roadmapImage
        val terrainImageCircle = _binding!!.terrainImage
        //Initialize the SDK
        Places.initializeWithNewPlacesApiEnabled(this.context!!, apiKey)

        // Create a new PlacesClient instance
        val placesClient = Places.createClient(this.context!!)
        //go to fullsail button
        fullsailButton.setOnClickListener {  map.animateCamera(CameraUpdateFactory.newCameraPosition(fullSail))}
        //clear searchbar when clearbutton is clicked
        clearButton.setOnClickListener { multiAutoCompleteTextView.text?.clear() }

        //map buttons
        satelliteButton.setOnClickListener {map.mapType=GoogleMap.MAP_TYPE_HYBRID
            satelliteImageCircle.setImageResource(R.drawable.green_circle)
            terrainImageCircle.setImageResource(R.drawable.grey_circle)
            roadImageCircle.setImageResource(R.drawable.grey_circle)
        }
        terrainButton.setOnClickListener {map.mapType=GoogleMap.MAP_TYPE_TERRAIN
            satelliteImageCircle.setImageResource(R.drawable.grey_circle)
            terrainImageCircle.setImageResource(R.drawable.green_circle)
            roadImageCircle.setImageResource(R.drawable.grey_circle)}
        roadButton.setOnClickListener {map.mapType=GoogleMap.MAP_TYPE_NORMAL
            satelliteImageCircle.setImageResource(R.drawable.grey_circle)
            terrainImageCircle.setImageResource(R.drawable.grey_circle)
            roadImageCircle.setImageResource(R.drawable.green_circle)}
        nearbySearchButton.setOnClickListener{
            locationCheckAndRequest()

            if (ActivityCompat.checkSelfPermission(
                    //if access fine location is granted
                    this.context!!,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                //and access coarse location is granted
                && ActivityCompat.checkSelfPermission(
                    this.context!!,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            )
            {
                val placeRequest = FindCurrentPlaceRequest.builder(mutableListOf(Place.Field.LOCATION)).build()
                val placeResponse = placesClient.findCurrentPlace(placeRequest)
                placeResponse.addOnSuccessListener { result ->
                    if(result.placeLikelihoods.isNotEmpty())
                    {
                        val location = result.placeLikelihoods[0].place.location
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 10f))

                        val circle : CircularBounds = circularBounds(location,30*1609.34)
                        //currentUser?.distance!!
                        val searchNearbyRequest = SearchNearbyRequest.builder(circle,mutableListOf(
                            Place.Field.ID, Place.Field.ADDRESS_COMPONENTS, Place.Field.LOCATION, Place.Field.DISPLAY_NAME))
                            .setIncludedTypes(listOf("hiking_area"))
                            .build()
                        placesClient.searchNearby(searchNearbyRequest).addOnSuccessListener{ result->
                            for(place in result.places)
                            {
                                map.addMarker(MarkerOptions().position(place.location).title(place.displayName))
                            }
                        }
                    }


                }.addOnFailureListener {
                }
            }

        }

        //dropdown menu adapter for list
        var autoFillAdapter =
            context?.let { ArrayAdapter(it, android.R.layout.simple_list_item_1, autocompletelistString) }
        multiAutoCompleteTextView.setAdapter(autoFillAdapter)
        multiAutoCompleteTextView.setTokenizer(MultiAutoCompleteTextView.CommaTokenizer())
        multiAutoCompleteTextView.threshold = 0


        //when user types in the searchbar
        multiAutoCompleteTextView.addTextChangedListener{
                updateAutoCompletePredictions(autoFillAdapter!!, multiAutoCompleteTextView.text.toString(), placesClient)
        }

        // Log an error if apiKey is not set.
        if (apiKey.isEmpty()) {
            Log.e("Places test", "No api key")
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //function to update list of autocomplete suggestions
    fun updateAutoCompletePredictions(adapter : ArrayAdapter<String>, string : String, placesClient : PlacesClient){
        val token = AutocompleteSessionToken.newInstance()
        val findAutocompletePredictionsRequest  =
            FindAutocompletePredictionsRequest.builder()
                .setQuery(string)
                .setTypesFilter(searchTypes)
                .setSessionToken(token)
                .build()
        placesClient.findAutocompletePredictions(findAutocompletePredictionsRequest).addOnSuccessListener{
            response ->
            autocompletelistString.clear()
            autocompletelist = response.autocompletePredictions
            adapter.clear()
            for (prediction in response.autocompletePredictions) {
                autocompletelistString.add(prediction.getFullText(null).toString())
                }
            adapter.addAll(autocompletelistString)
        }.addOnFailureListener {
            autocompletelistString.clear()
        }
    }
    override fun onCameraMoveStarted(p0: Int) {
        TODO("Not yet implemented")
    }

    override fun onCameraMove() {
        TODO("Not yet implemented")
    }

    override fun onCameraMoveCanceled() {
        TODO("Not yet implemented")
    }

    override fun onCameraIdle() {
        TODO("Not yet implemented")
    }

    override fun onMapReady(p0: GoogleMap) {
        map = p0
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.setAllGesturesEnabled(true)

        locationCheckAndRequest()
    }
    fun locationCheckAndRequest()
    {
        //request user location permissions
        if (ActivityCompat.checkSelfPermission(
                //if access fine location is not granted
                this.context!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            //and access coarse location is not granted
            && ActivityCompat.checkSelfPermission(
                this.context!!,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        )
        //request for permissions and override permissions result
        {
            ActivityCompat.requestPermissions(this.activity!!, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION), 1)
            onRequestPermissionsResult(1,arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION),
                intArrayOf(PackageManager.PERMISSION_GRANTED,PackageManager.PERMISSION_DENIED))
        }
    }
}
