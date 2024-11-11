package com.example.trailblaze.login

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.trailblaze.MainActivity
import com.example.trailblaze.R

class FifthPersonalizeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //inflate the layout
        val view = inflater.inflate(R.layout.fragment_fifth_personalize, container, false)
        view.findViewById<Button>(R.id.yesBtn).setOnClickListener {

            //navigate to MainActivity
            val intent = Intent(activity, MainActivity::class.java)
            //clear the activity stack so the user can't return to the fragments
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            activity?.finish()
        }

        //set up the finished button
        view.findViewById<Button>(R.id.LaterBtn).setOnClickListener {
            //navigate to MainActivity
            val intent = Intent(activity, MainActivity::class.java)
            //clear the activity stack so the user can't return to the fragments
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            activity?.finish()
        }
        return view
    }
}