package com.example.trailblaze.login

import android.content.Intent
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
import com.example.trailblaze.MainActivity
import com.example.trailblaze.R
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
            "Easy" to 2,
            "Moderate" to 3,
            "Difficult" to 4,
            "Extremely Difficulty" to 5,
        )

        val optionsSpinner4 = mapOf(
            "Day Hike" to 1,
            "Long Distance" to 2,
            "Peek Bagging" to 3
        )

        //set up the finished button
        view.findViewById<Button>(R.id.finished).setOnClickListener {
            //navigate to MainActivity
            val intent = Intent(activity, MainActivity::class.java)
            saveUserPreferences()
            //clear the activity stack so the user can't return to the fragments
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            activity?.finish()
        }
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