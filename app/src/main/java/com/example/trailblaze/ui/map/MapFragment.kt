package com.example.trailblaze.ui.Map

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.style.CharacterStyle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.Nullable
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import com.example.trailblaze.BuildConfig.PLACES_API_KEY
import com.example.trailblaze.R
import com.example.trailblaze.databinding.FragmentMapBinding
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.*
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.PlaceTypes.*
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.tasks.await
import java.util.*

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
    private var currPolylineOptions: PolylineOptions? = null
    private var isCanceled = false
    private val FullSail : CameraPosition = CameraPosition.builder().target(LatLng(28.596472,-81.301472)).zoom(15f).build()
    var autocompletelist : List<AutocompletePrediction> = mutableListOf()
    var autocompletelistString : MutableList<String> = mutableListOf("yo","bro","go","beep")
    val autocompletePrimaryTypes = listOf("hiking_area", "park")


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //Set the searchbar and clearbutton to their respective views
        var multiAutoCompleteTextView = _binding!!.mapSearch.findViewById<MultiAutoCompleteTextView>(R.id.map_search)
        var clearbutton = _binding!!.clearMapsearchtext.findViewById<ImageButton>(R.id.clear_mapsearchtext)
        //Initialize the SDK
        Places.initializeWithNewPlacesApiEnabled(context, apiKey)

        // Create a new PlacesClient instance
        val placesClient = Places.createClient(context)


        //clear searchbar when clearbutton is clicked
        clearbutton?.setOnClickListener { multiAutoCompleteTextView?.text?.clear() }

        var autoFillAdapter =
            context?.let { ArrayAdapter(it, android.R.layout.simple_list_item_1, autocompletelistString) }
        multiAutoCompleteTextView.setAdapter(autoFillAdapter)
        multiAutoCompleteTextView.setTokenizer(MultiAutoCompleteTextView.CommaTokenizer())
        multiAutoCompleteTextView.threshold = 0


        multiAutoCompleteTextView?.addTextChangedListener{

            if(multiAutoCompleteTextView.text?.isNotEmpty()==true) {
                UpdateMapDropDownMenu(multiAutoCompleteTextView.text.toString(), placesClient)
            }
        }

        // Log an error if apiKey is not set.
        if (apiKey.isEmpty()) {
            Log.e("Places test", "No api key")
            return root
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    fun UpdateMapDropDownMenu(string : String, placesClient: PlacesClient){

        UpdateAutoCompletePredictions(string, placesClient)


    }

    //function to update list of autocomplete suggestions
    fun UpdateAutoCompletePredictions(string : String, placesClient : PlacesClient){
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
            for (prediction in response.autocompletePredictions) {
                autocompletelistString.add(prediction.toString())
            }
        }.addOnFailureListener {autocompletelistString.clear()
            autocompletelistString.add("fail")}
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
        TODO("Not yet implemented")
    }
}