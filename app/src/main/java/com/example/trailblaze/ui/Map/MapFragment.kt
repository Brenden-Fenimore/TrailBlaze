package com.example.trailblaze.ui.Map

import android.Manifest
import android.content.Context
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
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.example.trailblaze.firestore.UserManager
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.kotlin.circularBounds
import com.google.android.libraries.places.api.net.*
import androidx.compose.material3.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.example.trailblaze.ui.Map.MapBottomSheetAdapter


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
    lateinit var autocompletelist : List<AutocompletePrediction>
    var autocompletelistString : MutableList<String> = mutableListOf()
    val searchTypes = listOf("hiking_area", "park")
    lateinit var mapFragment : SupportMapFragment
    var currentUser = UserManager.getCurrentUser()
    var placesList: MutableList<Place> = mutableListOf()
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    lateinit var thiscontext : Context
    lateinit var placesClient : PlacesClient
    lateinit var bottomSheetAdapter: MapBottomSheetAdapter
    lateinit var multiAutoCompleteTextView : MultiAutoCompleteTextView
    lateinit var autoFillAdapter : ArrayAdapter<String>



    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val root: View = binding.root
        thiscontext = this.context!!

        // Get the SupportMapFragment and request notification when the map is ready to be used.
        mapFragment = this.childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
        //Set interactive buttons to a value to work with
        multiAutoCompleteTextView = _binding!!.mapSearch
        val clearButton = _binding!!.clearMapsearchtext
        val fullsailButton = _binding!!.fullsail
        val satelliteButton = _binding!!.satellite
        val roadButton = _binding!!.roadmap
        val terrainButton = _binding!!.terrain
        val nearbySearchButton = _binding!!.nearbysearch
        val satelliteImageCircle = _binding!!.satelliteImage
        val roadImageCircle = _binding!!.roadmapImage
        val terrainImageCircle = _binding!!.terrainImage
        val recyclerView = _binding!!.bottomsheetinclude.bottomSheetRecycler

        //Initialize the SDK
        Places.initializeWithNewPlacesApiEnabled(thiscontext, apiKey)

        // Create a new PlacesClient instance
        placesClient = Places.createClient(thiscontext)
        //go to fullsail button
        fullsailButton.setOnClickListener {

//          map.animateCamera(CameraUpdateFactory.newCameraPosition(fullSail))
            placesList.clear()
            map.clear()
            bottomSheetAdapter = MapBottomSheetAdapter(placesList)
            recyclerView.adapter = bottomSheetAdapter
            recyclerView.layoutManager = LinearLayoutManager(thiscontext)
            recyclerView!!.adapter?.notifyDataSetChanged()
        }

        //clear searchbar when clearbutton is clicked
        clearButton.setOnClickListener { multiAutoCompleteTextView.text?.clear() }

        //map buttons
        roadButton.setOnClickListener {map.mapType=GoogleMap.MAP_TYPE_NORMAL
            roadImageCircle.setImageResource(R.drawable.green_circle)
            satelliteImageCircle.setImageResource(R.drawable.grey_circle)
            terrainImageCircle.setImageResource(R.drawable.grey_circle)}
        satelliteButton.setOnClickListener {map.mapType=GoogleMap.MAP_TYPE_HYBRID
            roadImageCircle.setImageResource(R.drawable.grey_circle)
            satelliteImageCircle.setImageResource(R.drawable.green_circle)
            terrainImageCircle.setImageResource(R.drawable.grey_circle)}
        terrainButton.setOnClickListener {map.mapType=GoogleMap.MAP_TYPE_TERRAIN
            roadImageCircle.setImageResource(R.drawable.grey_circle)
            satelliteImageCircle.setImageResource(R.drawable.grey_circle)
            terrainImageCircle.setImageResource(R.drawable.green_circle)}



        //Find trails near me button
        nearbySearchButton.setOnClickListener{
            //check permissions and request if no access is found
            locationCheckAndRequest()

            //check permission was granted before continuing
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
                map.clear()
                val placeRequest = FindCurrentPlaceRequest.builder(mutableListOf(Place.Field.LOCATION)).build()
                val placeResponse = placesClient.findCurrentPlace(placeRequest)
                placeResponse.addOnSuccessListener { result ->
                    if(result.placeLikelihoods.isNotEmpty())
                    {
                        //get the closest location to user
                        val location = result.placeLikelihoods[0].place.location
                        //animate camera to user location at 10 zoom
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 10f))
                        //refresh current user
                        currentUser = UserManager.getCurrentUser()
                        //take mile distance and convert to meters
                        var radius = currentUser!!.distance!!*1609.34
                        //check if meter distance is over 50,000 and if it is then set it to 50,000
                        if(radius > 50000.0) {radius = 50000.0 }
                        //create circle area to search within
                        val circle : CircularBounds = circularBounds(location,radius)
                        //create SearchNearbyRequest object
                        val searchNearbyRequest = SearchNearbyRequest.builder(circle,listOf(
                            Place.Field.ID,Place.Field.FORMATTED_ADDRESS, Place.Field.LOCATION, Place.Field.DISPLAY_NAME,Place.Field.PHOTO_METADATAS))
                            .setIncludedTypes(listOf("hiking_area"))
                            .build()
                        //on success of searchNearby function, create marker at each place with name
                        // and then add each place into placesList
                        placesClient.searchNearby(searchNearbyRequest).addOnSuccessListener{ result->
                            for(place in result.places)
                            {
                                map.addMarker(MarkerOptions().position(place.location).title(place.displayName))
                            }
                            placesList.addAll(result.places)

                            bottomSheetAdapter = MapBottomSheetAdapter(placesList)
                            recyclerView.adapter = bottomSheetAdapter
                            recyclerView.layoutManager = LinearLayoutManager(thiscontext)
                            recyclerView!!.adapter?.notifyDataSetChanged()

                        }
                    }


                }.addOnFailureListener {
                }
            }

        }

        //dropdown menu adapter for list
        autoFillAdapter =
            context!!.let { ArrayAdapter(it, android.R.layout.simple_list_item_1, autocompletelistString) }
        multiAutoCompleteTextView.setAdapter(autoFillAdapter)
        multiAutoCompleteTextView.setTokenizer(MultiAutoCompleteTextView.CommaTokenizer())
        multiAutoCompleteTextView.threshold = 0
        //if an item is clicked from the dropdown menu
        multiAutoCompleteTextView.setOnItemClickListener { parent, view, position, id ->
            //create a FetchPlaceRequest object that gets the place id from the list of places
            // and the list of fields we want to get more of
            val placeFetch : FetchPlaceRequest = FetchPlaceRequest.builder(autocompletelist[position].placeId.toString()
                ,listOf( Place.Field.ID, Place.Field.FORMATTED_ADDRESS, Place.Field.LOCATION, Place.Field.DISPLAY_NAME,Place.Field.PHOTO_METADATAS)).build()
            //ask for the place from Google and on success
            placesClient.fetchPlace(placeFetch).addOnSuccessListener{response ->
                //reset map and placeList, add place to placeList and move camera to the marker we
                // create from the place's location
                map.clear()
                placesList.clear()
                placesList.add(response.place)



                bottomSheetAdapter = MapBottomSheetAdapter(placesList)
                recyclerView.adapter = bottomSheetAdapter
                recyclerView.layoutManager = LinearLayoutManager(thiscontext)
                recyclerView!!.adapter?.notifyDataSetChanged()




                map.addMarker(MarkerOptions().position(response.place.location).title(response.place.displayName))
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(response.place.location,13f)) }
        }
        // Log an error if apiKey is not set.
        if (apiKey.isEmpty()) {
            Log.e("Places test", "No api key")
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){

        multiAutoCompleteTextView.addTextChangedListener{
                updateAutoCompletePredictions(autoFillAdapter!!, multiAutoCompleteTextView.text.toString(), placesClient)
        }
        val bottomsheet = this.view!!.findViewById<ConstraintLayout>(R.id.bottomsheetinclude)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomsheet)
        bottomSheetBehavior.maxHeight = 1400
        bottomSheetBehavior.peekHeight = 100
//        bottomSheetBehavior.addBottomSheetCallback(object :
//            BottomSheetBehavior.BottomSheetCallback() {
//
//            override fun onSlide(bottomSheet: View,     slideOffset:Float) {
//                // handle onSlide
//            }
//
//            override fun onStateChanged(bottomSheet: View, newState: Int) {
//                when (newState) {
//                    BottomSheetBehavior.STATE_COLLAPSED -> Toast.makeText(thiscontext, "STATE_COLLAPSED", Toast.LENGTH_SHORT).show()
//                    BottomSheetBehavior.STATE_EXPANDED -> Toast.makeText(thiscontext, "STATE_EXPANDED", Toast.LENGTH_SHORT).show()
//                    BottomSheetBehavior.STATE_DRAGGING -> Toast.makeText(thiscontext, "STATE_DRAGGING", Toast.LENGTH_SHORT).show()
//                    BottomSheetBehavior.STATE_SETTLING -> Toast.makeText(thiscontext, "STATE_SETTLING", Toast.LENGTH_SHORT).show()
//                    BottomSheetBehavior.STATE_HIDDEN -> Toast.makeText(thiscontext, "STATE_HIDDEN", Toast.LENGTH_SHORT).show()
//                    else -> Toast.makeText(thiscontext, "OTHER_STATE", Toast.LENGTH_SHORT).show()
//                }
//            }
//        })
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //function to update list of autocomplete suggestions
    fun updateAutoCompletePredictions(adapter : ArrayAdapter<String>, string : String, placesClient : PlacesClient){

        //create token for grouping requests for less cost
        val token = AutocompleteSessionToken.newInstance()

        //build the AutoCompletePredictionsRequest
        val findAutocompletePredictionsRequest  =
            FindAutocompletePredictionsRequest.builder()
                .setQuery(string)
                .setTypesFilter(searchTypes)
                .setSessionToken(token)
                .build()

        //use the findAP function from the Google places client and set on success listener
        placesClient.findAutocompletePredictions(findAutocompletePredictionsRequest).addOnSuccessListener{
            response ->

            //clear previous list of place names (strings)
            autocompletelistString.clear()

            //set our autocompletelist equal to the response's list
            autocompletelist = response.autocompletePredictions

            //clear the adapter
            adapter.clear()

            //loop through each prediction and grab the string for display
            for (prediction in response.autocompletePredictions) {
                autocompletelistString.add(prediction.getFullText(null).toString())
                }
            //add each string of the autocompletelistString to the adapter
            adapter.addAll(autocompletelistString)
            adapter.notifyDataSetChanged()
            //if request fails
        }.addOnFailureListener {
            //clear the string list so dropdown has no data
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
                thiscontext!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            //and access coarse location is not granted
            && ActivityCompat.checkSelfPermission(
                thiscontext!!,
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
