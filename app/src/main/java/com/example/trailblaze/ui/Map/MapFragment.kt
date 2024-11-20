package com.example.trailblaze.ui.Map

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.trailblaze.BuildConfig.PLACES_API_KEY
import com.example.trailblaze.R
import com.example.trailblaze.databinding.FragmentMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.*
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.example.trailblaze.ui.parks.ParkDetailActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.trailblaze.nps.RetrofitInstance
import com.example.trailblaze.nps.NPSResponse
import com.example.trailblaze.nps.Park
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLngBounds
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
    lateinit var userLocation: LatLng
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        thiscontext = requireContext()

        //Initialize the SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)

        binding.clearMapsearchtext.setOnClickListener {
            binding.mapSearch.setText("")  // Clear the text
            autocompletelistString.clear() // Clear suggestions
            autoFillAdapter.clear()        // Clear adapter
            autoFillAdapter.notifyDataSetChanged()
        }

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

    override fun onResume() {
        super.onResume()

        // Get the latest user data and configure search radius to meters
        val searchRadius = userDistanceMeters()

        // Update current map zoom if map is initialized
        _map?.let { googleMap ->
            val currentCenter = googleMap.cameraPosition.target
            val newZoomLevel = getZoomLevelForDistance(searchRadius)
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentCenter, newZoomLevel))
        }
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
        if (ActivityCompat.checkSelfPermission(
                thiscontext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                thiscontext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("MapFragment", "Location permissions granted")
            //get location and assign to userLocation val for kt scope to use for every function
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.context!!)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                map.isMyLocationEnabled = true // Shows the blue dot for current location
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude,location.longitude),15f))
                userLocation = LatLng(location.latitude,location.longitude)
            }
        }

    }


    private fun userDistanceMeters() : Double{
        // Get the latest user data
        currentUser = UserManager.getCurrentUser()
        val isMetricUnits = sharedPreferences.getBoolean("isMetricUnits", false)

        // Calculate the current search radius based on user settings
        val searchRadius = if (isMetricUnits) {
            currentUser?.distance?.times(1000.0) ?: 10000.0  // Convert km to meters
        } else {
            currentUser?.distance?.times(1609.34) ?: 10000.0  // Convert miles to meters
        }

        return searchRadius
    }

    private fun parkWithinBounds(userLocation: LatLng, searchRadius : Double, parkLocation: LatLng): Boolean {

        val searchRadiusLatLngRadius = searchRadius / 111111.1
        val bounds = LatLngBounds(
            //southwest corner
            LatLng(userLocation.latitude - searchRadiusLatLngRadius,
            userLocation.longitude - searchRadiusLatLngRadius),
            //northeast corner
            LatLng(userLocation.latitude + searchRadiusLatLngRadius,
            userLocation.longitude + searchRadiusLatLngRadius))

        return bounds.contains(parkLocation)
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
            threshold = 1  // Change threshold to 1 character

            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length ?: 0 >= 1) {
                        updateAutoCompletePredictions(autoFillAdapter, s.toString(), placesClient)
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            setOnItemClickListener { _, _, position, _ ->
                handleSearchItemClick(position)
            }
        }
    }


    private fun handleMarkerClick(marker: Marker): Boolean {
        // Handle park markers
        val park = marker.tag as? Park
        if (park != null) {
            navigateToDetail(parkCode = park.parkCode)
            return true
        }

        // Handle place markers
        val place = marker.tag as? Place
        if (place != null) {
            navigateToDetail(placeId = place.id)
            return true
        }

        return false
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
                        navigateToDetail(placeId = locationItem.place.id)
                    }
                    is LocationItem.ParkItem -> {
                        navigateToDetail(parkCode = locationItem.park.parkCode)
                    }
                }
            }
        )

        binding.bottomsheetinclude.bottomSheetRecycler.apply {
            adapter = bottomSheetAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    fun updateAutoCompletePredictions(adapter: ArrayAdapter<String>, string: String, placesClient: PlacesClient) {
        val token = AutocompleteSessionToken.newInstance()

        val findAutocompletePredictionsRequest = FindAutocompletePredictionsRequest.builder()
            .setQuery(string)
            .setTypesFilter(searchTypes)
            .setSessionToken(token)
            .build()

        placesClient.findAutocompletePredictions(findAutocompletePredictionsRequest)
            .addOnSuccessListener { response ->
                autocompletelistString.clear()
                autocompletelist = response.autocompletePredictions

                response.autocompletePredictions.forEach { prediction ->
                    autocompletelistString.add(prediction.getFullText(null).toString())
                }

                adapter.clear()
                adapter.addAll(autocompletelistString)

                // Force the dropdown to show if there are results
                if (autocompletelistString.isNotEmpty()) {
                    multiAutoCompleteTextView.showDropDown()
                }
            }
            .addOnFailureListener {
                autocompletelistString.clear()
                adapter.clear()
                adapter.notifyDataSetChanged()
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


    private fun fetchParksAndPlaceMarkers(userState: String) {
        RetrofitInstance.api.getParksbyState(stateCode = userState).enqueue(object : Callback<NPSResponse> {
            override fun onResponse(call: Call<NPSResponse>, response: Response<NPSResponse>) {
                if (response.isSuccessful) {
                    val parksList = response.body()?.data ?: emptyList()
                    map.clear()
                    val beforeFilterItems = parksList.map { LocationItem.ParkItem(it) }

                    userDistanceMeters()
                    val newLocationItems : MutableList<LocationItem.ParkItem> = mutableListOf()
                    beforeFilterItems.forEach { park ->
                        if (park.park.latitude != "" || park.park.longitude != "" || !::userLocation.isInitialized) {
                            if (parkWithinBounds(
                                    userLocation, userDistanceMeters(),
                                    LatLng(park.park.latitude!!.toDouble(), park.park.longitude!!.toDouble()))
                            )
                            {
                                newLocationItems.add(park)
                            }
                        }
                    }
                    locationItems.clear()
                    locationItems.addAll(newLocationItems)
                    bottomSheetAdapter.updateItems(locationItems)

                    if (newLocationItems.isNotEmpty()) {
                        val firstPark = parksList[0]
                        val latitude = firstPark.latitude?.toDoubleOrNull()
                        val longitude = firstPark.longitude?.toDoubleOrNull()

                        if (latitude != null && longitude != null) {
                            val parkLocation = LatLng(latitude, longitude)

                            val distanceInMeters = userDistanceMeters()

                            // Calculate zoom level based on user's distance setting
                            val zoomLevel = getZoomLevelForDistance(distanceInMeters)

                            // Apply the zoom level
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(parkLocation, zoomLevel))

                            // Add markers for parks
                            for (park in newLocationItems) {
                                val lat = park.park.latitude?.toDoubleOrNull()
                                val lon = park.park.longitude?.toDoubleOrNull()

                                if (lat != null && lon != null) {
                                    val marker = map.addMarker(
                                        MarkerOptions()
                                            .position(LatLng(lat, lon))
                                            .title(park.park.fullName)
                                    )
                                    marker?.tag = park
                                }
                            }
                        }
                    }
                    else
                    {
                        Toast.makeText(context, "No National Parks within your search radius", Toast.LENGTH_SHORT).show()
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
                roadmapImage.setImageResource(R.drawable.circle_for_map_type_grey)
                satelliteImage.setImageResource(R.drawable.circle_for_map_type_green)
                terrainImage.setImageResource(R.drawable.circle_for_map_type_grey)
            }

            roadmap.setOnClickListener {
                map.mapType = GoogleMap.MAP_TYPE_NORMAL
                roadmapImage.setImageResource(R.drawable.circle_for_map_type_green)
                satelliteImage.setImageResource(R.drawable.circle_for_map_type_grey)
                terrainImage.setImageResource(R.drawable.circle_for_map_type_grey)
            }

            terrain.setOnClickListener {
                map.mapType = GoogleMap.MAP_TYPE_TERRAIN
                roadmapImage.setImageResource(R.drawable.circle_for_map_type_grey)
                satelliteImage.setImageResource(R.drawable.circle_for_map_type_grey)
                terrainImage.setImageResource(R.drawable.circle_for_map_type_green)
            }

            npsnearbysearch.setOnClickListener {
                // Get the metric/imperial preference
                if (ActivityCompat.checkSelfPermission(
                        thiscontext,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(
                        thiscontext,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    map.clear()
                    val placeRequest = FindCurrentPlaceRequest.builder(mutableListOf(Place.Field.LAT_LNG)).build()
                    val placeResponse = placesClient.findCurrentPlace(placeRequest)

                    placeResponse.addOnSuccessListener { result ->
                        if (result.placeLikelihoods.isNotEmpty()) {
                            val userLocation = currentUser?.state ?: ""
                            if (userLocation != null) {
                                fetchParksAndPlaceMarkers(userLocation)
                            }
                        }
                    }.addOnFailureListener {
                        Log.e("MapFragment", "Failed to get user's current location")
                    }
                }
            }

            nearbysearch.setOnClickListener {
                Log.d("MapFragment", "Nearby search clicked")
                locationCheckAndRequest()

                if (ActivityCompat.checkSelfPermission(
                        thiscontext,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(
                        thiscontext,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    Log.d("MapFragment", "Location permissions granted")
                    map.clear()
                    val placeRequest = FindCurrentPlaceRequest.builder(mutableListOf(Place.Field.LOCATION)).build()
                    val placeResponse = placesClient.findCurrentPlace(placeRequest)
                    placeResponse.addOnSuccessListener { result ->
                        Log.d("MapFragment", "Found current place, likelihoods size: ${result.placeLikelihoods.size}")
                        if (result.placeLikelihoods.isNotEmpty()) {
                            Log.d("MapFragment", "User location: ${userLocation.latitude}, ${userLocation.longitude}")

                            currentUser = UserManager.getCurrentUser()

                            // Convert the distance based on the unit setting
                            var radius = userDistanceMeters()
                            if (radius > 50000.0){radius = 50000.0}
                            Log.d("MapFragment", "Search radius: $radius meters")

                            // Calculate appropriate zoom level
                            val zoomLevel = getZoomLevelForDistance(radius)

                            // Apply the zoom level when moving camera
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, zoomLevel))

                            val circle: CircularBounds = circularBounds(userLocation, radius)
                            val searchNearbyRequest = SearchNearbyRequest.builder(
                                circle, listOf(
                                    Place.Field.ID,
                                    Place.Field.FORMATTED_ADDRESS,
                                    Place.Field.LOCATION,
                                    Place.Field.DISPLAY_NAME,
                                    Place.Field.PHOTO_METADATAS
                                )
                            )
                                .setIncludedTypes(listOf("hiking_area"))
                                .build()

                            placesClient.searchNearby(searchNearbyRequest).addOnSuccessListener { result ->
                                Log.d("MapFragment", "Found ${result.places.size} nearby places")

                                val newLocationItems = result.places.map { LocationItem.PlaceItem(it) }
                                locationItems.clear()
                                locationItems.addAll(newLocationItems)
                                bottomSheetAdapter.updateItems(locationItems)

                                for (place in result.places) {
                                    Log.d("MapFragment", "Adding marker for place: ${place.displayName}")
                                    val marker = map.addMarker(
                                        MarkerOptions()
                                            .position(place.location)
                                            .title(place.displayName)
                                    )
                                    marker?.tag = place
                                }
                            }.addOnFailureListener { e ->
                                Log.e("MapFragment", "Nearby search failed: ${e.message}")
                            }
                        }
                    }.addOnFailureListener { e ->
                        Log.e("MapFragment", "Finding current place failed: ${e.message}")
                    }
                } else {
                    Log.d("MapFragment", "Location permissions not granted")
                }
            }
        }
    }

    private fun navigateToDetail(parkCode: String? = null, placeId: String? = null) {
        val intent = Intent(context, ParkDetailActivity::class.java).apply {
            parkCode?.let { putExtra("PARK_CODE", it) }
            placeId?.let { putExtra("PLACE_ID", it) }
        }
        startActivity(intent)
    }

    // Function to calculate zoom level based on distance
    private fun getZoomLevelForDistance(distanceInMeters: Double): Float {
        // Formula provides a reasonable zoom level based on distance
        // Zoom levels: 1 = World, 5 = Landmass/continent, 10 = City, 15 = Streets, 20 = Buildings
        return when {
            distanceInMeters >= 50000 -> 8f    // Max distance (50km)
            distanceInMeters >= 40000 -> 9f
            distanceInMeters >= 30000 -> 9.5f
            distanceInMeters >= 20000 -> 10f
            distanceInMeters >= 10000 -> 11f
            distanceInMeters >= 5000 -> 12f
            distanceInMeters >= 2000 -> 13f
            distanceInMeters >= 1000 -> 14f
            else -> 15f                        // Very close distance
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