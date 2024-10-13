package com.example.trailblaze.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import com.example.trailblaze.R
import com.example.trailblaze.ui.UserPreferences.UserPreferences
import com.google.firebase.auth.FirebaseAuth

class SecondPersonalizeFragment : Fragment() {

    //declaration
    private lateinit var city : EditText
    private lateinit var state: EditText
    private lateinit var zip : EditText

    lateinit var seekBar: SeekBar
    lateinit var selectedValueTextView: TextView
    private var selectedFilterValue: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        //inflate the layout
        val view = inflater.inflate(R.layout.fragment_second_personalize, container, false)
        view.findViewById<Button>(R.id.continuebtn2).setOnClickListener {
            //navigate to ThirdFragment
            (activity as? PersonalizeActivity)?.loadFragment(ThirdPersonalizeFragment())
        }
        // Initialize views
        city = view.findViewById(R.id.etCity)
        state = view.findViewById(R.id.etState)
        zip = view.findViewById(R.id.etZip)
        seekBar = view.findViewById(R.id.seekBar)
        selectedValueTextView = view.findViewById(R.id.range)

        // Set up the SeekBar listener
        setupSeekBarListener()

        return view
    }
    private fun setupSeekBarListener() {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                //update the TextView with the current progress/value of the seekBar
                selectedValueTextView.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Optional: Do something when the user starts dragging the SeekBar
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                //get the progress value when the user stops dragging the SeekBar
                selectedFilterValue = (seekBar?.progress ?: 0.0).toDouble()

            }
        })
    }
    private fun saveUserPreferences() {
        // Get user input
        val userCity = city.text.toString()
        val userState = state.text.toString()
        val userZip = zip.text.toString()
        val userDistance = selectedFilterValue  // The distance selected by the user
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            // Create a UserPreferences object
            val userPreferences = UserPreferences(
                city = userCity,
                state = userState,
                zip = userZip,
                distance = userDistance.toDouble()
            )
        }
    }
}