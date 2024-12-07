package com.byteforce.trailblaze.login

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import com.byteforce.trailblaze.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FourthPersonalizeFragment : Fragment() {
    //declaration
    private lateinit var terrain: Spinner
    private lateinit var fitnessLevel: Spinner
    private lateinit var difficulty: Spinner
    private lateinit var typeOfHike: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //inflate the layout
        val view = inflater.inflate(R.layout.fragment_fourth_personalize, container, false)
        view.findViewById<Button>(R.id.continuebtn4).setOnClickListener {
            //navigate to ThirdFragment
            (activity as? PersonalizeActivity)?.loadFragment(FifthPersonalizeFragment())
        }
        // define map for spinner options with unique ids
        val optionsSpinner1 = mapOf(
            "Foot Trails" to 1,
            "Nature Trails" to 2,
            "Rock Trails" to 3,
        )

        val optionsSpinner2 = mapOf(
            "Low-Intensity" to 1,
            "Moderate-Intensity" to 2,
            "High-Intensity" to 3,

        )

        val optionsSpinner3 = mapOf(
            "Easiest" to 1,
            "Moderate" to 2,
            "Moderately Strenuous" to 3,
            "Strenuous" to 4,
            "Very Strenuous" to 5,
        )

        val optionsSpinner4 = mapOf(
            "Day Hike" to 1,
            "Backpacking" to 2,
            "Trail Running" to 3,
            "Climbing" to 4,
        )

        //initialize the Spinners
        terrain = view.findViewById(R.id.spinner1)
        fitnessLevel = view.findViewById(R.id.spinner2)
        difficulty = view.findViewById(R.id.spinner3)
        typeOfHike = view.findViewById(R.id.spinner4)

       //create function to populate the spinner with options
        fun populateSpinner(spinner: Spinner, options: Map<String, Int>) {
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options.keys.toList())
           adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
           spinner.adapter = adapter
        }

        //populate each spinner
        populateSpinner(terrain, optionsSpinner1)
        populateSpinner(fitnessLevel, optionsSpinner2)
        populateSpinner(difficulty, optionsSpinner3)
        populateSpinner(typeOfHike, optionsSpinner4)

        // Retrieve selected option and its ID
        terrain.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedOption = parent.getItemAtPosition(position) as String
                val selectedId = optionsSpinner1[selectedOption]
                Log.d("Selected Option", "Option: $selectedOption, ID: $selectedId")
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        return view
    }

    private fun saveUserPreferences() {

        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Get spinner selections
        val selectedTerrain = terrain.selectedItem.toString()
        val selectedFitnessLevel = fitnessLevel.selectedItem.toString()
        val selectedDifficulty = difficulty.selectedItem.toString()
        val selectedTypeOfHike = typeOfHike.selectedItem.toString()

        if (userId != null) {
            // Create a Firestore reference
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(userId)

            // Create a map to store the user preferences
            val userPreferences = hashMapOf(
                "terrain" to selectedTerrain,
                "fitnessLevel" to selectedFitnessLevel,
                "difficulty" to selectedDifficulty,
                "typeOfHike" to selectedTypeOfHike
            )

            // Update the user document with the preferences
            userRef.update(userPreferences as Map<String, Any>)
                .addOnSuccessListener {
                    // Data saved successfully
                    Log.d("Firebase", "User preferences saved successfully")
                    // Navigate to the next fragment
                    // (activity as? PersonalizeActivity)?.loadFragment(ThirdPersonalizeFragment())
                }
                .addOnFailureListener { exception ->
                    // Error saving data
                    Log.w("Firebase", "Error saving user preferences", exception)
                }
        }
    }
}