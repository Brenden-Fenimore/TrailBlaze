package com.example.trailblaze.ui.Map

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.trailblaze.BuildConfig.PLACES_API_KEY
import com.example.trailblaze.R
import com.example.trailblaze.databinding.FragmentMapBinding
import com.example.trailblaze.settings.SettingsScreenActivity
import com.google.android.libraries.places.api.Places

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    //Define a variable to hold the Places API key.
    val apiKey = PLACES_API_KEY







    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Log an error if apiKey is not set.
        if (apiKey.isEmpty() || apiKey == "DEFAULT_API_KEY") {
            Log.e("Places test", "No api key")
            return root
         }

         //Initialize the SDK
        Places.initialize(context, apiKey)

        // Create a new PlacesClient instance
        val placesClient = Places.createClient(context)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}