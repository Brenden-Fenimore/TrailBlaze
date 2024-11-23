package com.example.trailblaze.login

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import com.example.trailblaze.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SecondPersonalizeFragment : Fragment() {

    //declaration
    private lateinit var city: EditText
    private lateinit var state: EditText
    private lateinit var zip: EditText

    private lateinit var seekBar: SeekBar
    lateinit var selectedValueTextView: TextView
    private var selectedFilterValue: Double = 0.0
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        //inflate the layout
        val view = inflater.inflate(R.layout.fragment_second_personalize, container, false)

        view.findViewById<Button>(R.id.continuebtn2).setOnClickListener {
            saveUserPreferences()
            //navigate to ThirdFragment
            (activity as? PersonalizeActivity)?.loadFragment(ThirdPersonalizeFragment())
        }

        sharedPreferences = requireActivity().getSharedPreferences(
            "app_preferences",  // Your preference file name
            Context.MODE_PRIVATE
        )
        // Initialize views
        city = view.findViewById(R.id.etCity)
        state = view.findViewById(R.id.etState)
        zip = view.findViewById(R.id.etZip)
        seekBar = view.findViewById(R.id.seekBar)
        selectedValueTextView = view.findViewById(R.id.range)

        // Set up state EditText filters
        setupStateEditText()
        // Set up the SeekBar listener
        setupSeekBarListener()

        return view
    }

    private fun setupStateEditText() {
        // Set max length to 2 characters
        state.filters = arrayOf(InputFilter.LengthFilter(2))

        // Convert input to uppercase while typing
        state.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s != null && !s.toString().isEmpty()) {
                    val uppercase = s.toString().uppercase()
                    if (uppercase != s.toString()) {
                        state.setText(uppercase)
                        state.setSelection(uppercase.length)
                    }
                }
            }
        })
    }

    private fun setupSeekBarListener() {
        seekBar.apply {
            max = 50  // Maximum distance in km/miles
            progress = 10  // Default value

            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    // Ensure minimum value of 1
                    val actualProgress = if (progress < 1) 1 else progress
                    selectedFilterValue = actualProgress.toDouble()
                    updateSeekBarLabel(actualProgress)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
    }

    private fun updateSeekBarLabel(progress: Int) {
        val isMetric = sharedPreferences.getBoolean("isMetricUnits", true)
        val distance = if (isMetric) {
            "$progress km"  // Metric (kilometers)
        } else {
            "${(progress * 0.621371).toInt()} miles"  // Imperial (miles)
        }

        // Add zoom level reference
        val zoomLevel = when (progress) {
            in 1..2 -> "Street level view"
            in 3..5 -> "Neighborhood view"
            in 6..10 -> "City view"
            in 11..20 -> "Regional view"
            in 21..35 -> "State view"
            else -> "Wide area view"
        }

        selectedValueTextView.text = "$distance ($zoomLevel)"
    }

    private fun saveUserPreferences() {
        // Get user input
        val userCity = city.text.toString()
        val userState = state.text.toString()
        val userZip = zip.text.toString()
        val userDistance = selectedFilterValue  // The distance selected by the user
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            // Create a Firestore reference
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(userId)

            // Create a map to store the user preferences
            val userPreferences = hashMapOf(
                "city" to userCity,
                "state" to userState,
                "zip" to userZip,
                "distance" to userDistance
            )

            // Update the user document with the preferences
            userRef.update(userPreferences as Map<String, Any>)
                .addOnSuccessListener {
                    // Data saved successfully
                    Log.d("Firebase", "User preferences saved successfully")
                    // Navigate to the next fragment
                    (activity as? PersonalizeActivity)?.loadFragment(ThirdPersonalizeFragment())
                }
                .addOnFailureListener { exception ->
                    // Error saving data
                    Log.w("Firebase", "Error saving user preferences", exception)
                }
        }
    }
}
