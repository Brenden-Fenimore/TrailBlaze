package com.example.trailblaze.ui.Map

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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
    val autocompletePrimaryTypes = listOf("hiking_area", "park")
    lateinit var mapFragment : SupportMapFragment





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

        //Initialize the SDK
        Places.initializeWithNewPlacesApiEnabled(this.context!!, apiKey)

        // Create a new PlacesClient instance
        val placesClient = Places.createClient(this.context!!)


        //go to fullsail button
        fullsailButton.setOnClickListener {  map.animateCamera(CameraUpdateFactory.newCameraPosition(fullSail))}

        //clear searchbar when clearbutton is clicked
        clearButton.setOnClickListener { multiAutoCompleteTextView.text?.clear() }

        //map buttons
        satelliteButton.setOnClickListener {map.mapType=GoogleMap.MAP_TYPE_HYBRID}
        terrainButton.setOnClickListener {map.mapType=GoogleMap.MAP_TYPE_TERRAIN}
        roadButton.setOnClickListener {map.mapType=GoogleMap.MAP_TYPE_NORMAL}


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
                .setTypesFilter(autocompletePrimaryTypes)
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
    }
}
