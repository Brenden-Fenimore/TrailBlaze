package com.example.trailblaze.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.trailblaze.R
import com.example.trailblaze.databinding.FragmentEditWatcherProfileBinding
import com.example.trailblaze.watcherFeature.EditWatcherProfile

class FifthPersonalizeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //inflate the layout
        val view = inflater.inflate(R.layout.fragment_fifth_personalize, container, false)
        view.findViewById<Button>(R.id.yesBtn).setOnClickListener {
            //Navigate to MainActivity
            (activity as? PersonalizeActivity)?.loadFragment(FragmentEditWatcherProfileBinding)
        }

        view.findViewById<Button>(R.id.LaterBtn).setOnClickListener {
            // Navigate to MainActivity
        }
        return view
    }
}