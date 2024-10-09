package com.example.trailblaze.Login

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import com.example.trailblaze.MainActivity
import com.example.trailblaze.R

class FourthPersonalizeFragment : Fragment() {
    //declaration
    private lateinit var spinner1: Spinner
    private lateinit var spinner2: Spinner
    private lateinit var spinner3: Spinner
    private lateinit var spinner4: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //inflate the layout
        val view = inflater.inflate(R.layout.fragment_fourth_personalize, container, false)

        //set up the finished button
        view.findViewById<Button>(R.id.finished).setOnClickListener {
            //navigate to MainActivity
            val intent = Intent(activity, MainActivity::class.java)
            //clear the activity stack so the user can't return to the fragments
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            activity?.finish()
        }
        //initalize the Spinners
        spinner1 = view.findViewById(R.id.spinner1)
        spinner2 = view.findViewById(R.id.spinner2)
        spinner3 = view.findViewById(R.id.spinner3)
        spinner4 = view.findViewById(R.id.spinner4)

        //options for spinner 1
        val optionsSpinner1 = arrayOf("Foot Trails", "Nature Trails", "Rocky Trails")
        //options for spinner 2
        val optionsSpinner2 = arrayOf("Low-Intensity", "Moderate-Intensity", "High-Intensity")
        //options for spinner 3
        val optionsSpinner3 = arrayOf("Easiest", "Easy", "Moderate", "Difficult", "Exremely Difficult")
        //options for spinner 4
        val optionsSpinner4 = arrayOf("Day Hike", "Long Distance", "Peek Bagging")

        //set the adapter for spinner 1
        val adapter1 = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, optionsSpinner1)
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner1.adapter = adapter1

        //set the adapter for spinner 2
        val adapter2 = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, optionsSpinner2)
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner2.adapter = adapter2

        //set the adapter for spinner 2
        val adapter3 = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, optionsSpinner3)
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner3.adapter = adapter3

        //set the adapter for spinner 2
        val adapter4 = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, optionsSpinner4)
        adapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner4.adapter = adapter4

        return view
    }
}