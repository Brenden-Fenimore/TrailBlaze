package com.example.trailblaze.ui.Map

import android.Manifest
import android.content.Context
import android.content.Intent
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
import com.example.trailblaze.nps.ParksAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.example.trailblaze.ui.Map.MapBottomSheetAdapter
import com.example.trailblaze.ui.parks.ParkDetailActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.trailblaze.nps.RetrofitInstance
import com.example.trailblaze.nps.NPSResponse
import com.example.trailblaze.nps.Park
import com.google.android.gms.maps.model.Marker

class MapFragment : Fragment(),
    OnCameraMoveStartedListener,
    OnCameraMoveListener,
    OnCameraMoveCanceledListener,
    OnCameraIdleListener,
    OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private var _map: GoogleMap? = null
    private val map get() = _map!!

    val apiKey = PLACES_API_KEY
    private val fullSail = CameraPosition.builder()
        .target(LatLng(28.596472,-81.301472))
        .zoom(15f)
        .build()

    lateinit var autocompletelist: List<AutocompletePrediction>
    var autocompletelistString: MutableList<String> = mutableListOf()
    val searchTypes = listOf("hiking_area", "park")
    lateinit var mapFragment: SupportMapFragment
    var currentUser = UserManager.getCurrentUser()
    var locationItems: MutableList<LocationItem> = mutableListOf()
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    lateinit var thiscontext: Context
    lateinit var placesClient: PlacesClient
    lateinit var bottomSheetAdapter: MapBottomSheetAdapter
    lateinit var multiAutoCompleteTextView: MultiAutoCompleteTextView
    lateinit var autoFillAdapter: ArrayAdapter<String>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        thiscontext = requireContext()

        // Initialize Places SDK first
        setupPlacesSDK()

        // Then initialize views (which includes setupBottomSheetAdapter)
        initializeViews()

        // Initialize map fragment
        mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupClickListeners()
        setupSearchFunctionality()

        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        _map = googleMap

        // Configure map settings
        map.apply {
            uiSettings.isZoomControlsEnabled = true
            uiSettings.setAllGesturesEnabled(true)

            setOnMarkerClickListener { marker ->
                handleMarkerClick(marker)
                true
            }
        }

        locationCheckAndRequest()
    }

    private fun initializeViews() {
        multiAutoCompleteTextView = binding.mapSearch
        setupBottomSheetAdapter()
        setupBottomSheet()
    }

    private fun setupPlacesSDK() {
        Places.initializeWithNewPlacesApiEnabled(thiscontext, apiKey)
        placesClient = Places.createClient(thiscontext)
    }

    private fun setupSearchFunctionality() {
        autoFillAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, autocompletelistString)

        with(multiAutoCompleteTextView) {
            setAdapter(autoFillAdapter)
            setTokenizer(MultiAutoCompleteTextView.CommaTokenizer())
            threshold = 0

            addTextChangedListener {
                updateAutoCompletePredictions(autoFillAdapter, text.toString(), placesClient)
            }

            setOnItemClickListener { _, _, position, _ ->
                handleSearchItemClick(position)
            }
        }
    }


    private fun handleMarkerClick(marker: Marker): Boolean {
        // First try to get the park from the marker's tag
        val park = marker.tag as? Park
        if (park != null) {
            val intent = Intent(context, ParkDetailActivity::class.java).apply {
                putExtra("PARK_CODE", park.parkCode)
            }
            startActivity(intent)
            return true
        }

        // If it's not a park, check if it's a place
        val matchingPlace = locationItems.filterIsInstance<LocationItem.PlaceItem>()
            .firstOrNull { it.place.location == marker.position }

        matchingPlace?.let {
            val intent = Intent(context, ParkDetailActivity::class.java).apply {
                putExtra("PLACE_ID", it.place.id)
            }
            startActivity(intent)
        }

        return true
    }
    private fun handleSearchItemClick(position: Int) {
        val placeFetch = FetchPlaceRequest.builder(
            autocompletelist[position].placeId,
            listOf(Place.Field.ID, Place.Field.FORMATTED_ADDRESS,
                Place.Field.LOCATION, Place.Field.DISPLAY_NAME,
                Place.Field.PHOTO_METADATAS)
        ).build()

        placesClient.fetchPlace(placeFetch).addOnSuccessListener { response ->
            _map?.clear()
            locationItems.clear()
            locationItems.add(LocationItem.PlaceItem(response.place))
            bottomSheetAdapter.updateItems(locationItems)

            _map?.addMarker(
                MarkerOptions()
                    .position(response.place.location)
                    .title(response.place.displayName)
            )
            _map?.animateCamera(CameraUpdateFactory.newLatLngZoom(response.place.location, 13f))
        }
    }
    private fun setupBottomSheetAdapter() {
        bottomSheetAdapter = MapBottomSheetAdapter(
            placesClient = placesClient,
            onItemClick = { locationItem ->
                when (locationItem) {
                    is LocationItem.PlaceItem -> {
                        val place = locationItem.place
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(place.location, 15f))
                    }
                    is LocationItem.ParkItem -> {
                        val park = locationItem.park
                        val intent = Intent(context, ParkDetailActivity::class.java).apply {
                            putExtra("PARK_CODE", park.parkCode)
                        }
                        startActivity(intent)
                    }
                }
            }
        )

        binding.bottomsheetinclude.bottomSheetRecycler.apply {
            adapter = bottomSheetAdapter
            layoutManager = LinearLayoutManager(context)
        }
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

    private fun setupBottomSheet() {
        val bottomsheet = binding.bottomsheetinclude.root
        bottomSheetBehavior = BottomSheetBehavior.from(bottomsheet)
        bottomSheetBehavior.maxHeight = 1400
        bottomSheetBehavior.peekHeight = 100
    }

    private fun startParkDetailActivity(parkCode: String? = null, placeId: String? = null) {
        val intent = Intent(context, ParkDetailActivity::class.java).apply {
            parkCode?.let { putExtra("PARK_CODE", it) }
            placeId?.let { putExtra("PLACE_ID", it) }
        }
        startActivity(intent)
    }

    private fun fetchParksAndPlaceMarkers(userState: String) {
        RetrofitInstance.api.getParksbyQuery(searchTerm = userState).enqueue(object : Callback<NPSResponse> {
            override fun onResponse(call: Call<NPSResponse>, response: Response<NPSResponse>) {
                if (response.isSuccessful) {
                    val parksList = response.body()?.data ?: emptyList()
                    map.clear()
                    val newLocationItems = parksList.map { LocationItem.ParkItem(it) }
                    locationItems.clear()
                    locationItems.addAll(newLocationItems)
                    bottomSheetAdapter.updateItems(locationItems)

                    if (parksList.isNotEmpty()) {
                        val firstPark = parksList[0]
                        val latitude = firstPark.latitude?.toDoubleOrNull()
                        val longitude = firstPark.longitude?.toDoubleOrNull()

                        if (latitude != null && longitude != null) {
                            val parkLocation = LatLng(latitude, longitude)
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(parkLocation, 10f))

                            for (park in parksList) {
                                val lat = park.latitude?.toDoubleOrNull()
                                val lon = park.longitude?.toDoubleOrNull()

                                if (lat != null && lon != null) {
                                    val marker = map.addMarker(
                                        MarkerOptions()
                                            .position(LatLng(lat, lon))
                                            .title(park.fullName)
                                    )
                                    marker?.tag = park
                                }
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call<NPSResponse>, t: Throwable) {
                Toast.makeText(context, "Failed to fetch parks", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupClickListeners() {
        with(binding) {

            satellite.setOnClickListener {
                map.mapType = GoogleMap.MAP_TYPE_HYBRID
                roadmapImage.setImageResource(R.drawable.grey_circle)
                satelliteImage.setImageResource(R.drawable.green_circle)
                terrainImage.setImageResource(R.drawable.grey_circle)
            }

            roadmap.setOnClickListener {
                map.mapType = GoogleMap.MAP_TYPE_NORMAL
                roadmapImage.setImageResource(R.drawable.green_circle)
                satelliteImage.setImageResource(R.drawable.grey_circle)
                terrainImage.setImageResource(R.drawable.grey_circle)
            }

            terrain.setOnClickListener {
                map.mapType = GoogleMap.MAP_TYPE_TERRAIN
                roadmapImage.setImageResource(R.drawable.grey_circle)
                satelliteImage.setImageResource(R.drawable.grey_circle)
                terrainImage.setImageResource(R.drawable.green_circle)
            }

            npsnearbysearch.setOnClickListener {
                if (ActivityCompat.checkSelfPermission(thiscontext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(thiscontext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                ) {
                    map.clear()
                    val placeRequest = FindCurrentPlaceRequest.builder(mutableListOf(Place.Field.LAT_LNG)).build()
                    val placeResponse = placesClient.findCurrentPlace(placeRequest)

                    placeResponse.addOnSuccessListener { result ->
                        if (result.placeLikelihoods.isNotEmpty()) {
                            val userLocation = currentUser?.state ?: ""
                            if (userLocation != null) {
                                currentUser = UserManager.getCurrentUser()
                                fetchParksAndPlaceMarkers(userLocation)
                            }
                        }
                    }.addOnFailureListener {
                        Log.e("MapFragment", "Failed to get user's current location")
                    }
                }
            }

            nearbysearch.setOnClickListener {
                locationCheckAndRequest()

                if (ActivityCompat.checkSelfPermission(thiscontext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(thiscontext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                ) {
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

                            //TODO (check if measurement of distance is metric or imperial before converting)
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
                            placesClient.searchNearby(searchNearbyRequest).addOnSuccessListener { result ->
                                val newLocationItems = result.places.map { LocationItem.PlaceItem(it) }
                                locationItems.clear()
                                locationItems.addAll(newLocationItems)
                                bottomSheetAdapter.updateItems(locationItems)

                                for(place in result.places) {
                                    val marker = map.addMarker(
                                        MarkerOptions()
                                            .position(place.location)
                                            .title(place.displayName)
                                    )
                                    marker?.tag = place  // Set the place as the marker's tag
                                }
                            }
                        }


                    }.addOnFailureListener {
                    }
                }

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _map = null
    }

    // Implement your camera movement callbacks as needed
    override fun onCameraMoveStarted(p0: Int) {}
    override fun onCameraMove() {}
    override fun onCameraMoveCanceled() {}
    override fun onCameraIdle() {}
}