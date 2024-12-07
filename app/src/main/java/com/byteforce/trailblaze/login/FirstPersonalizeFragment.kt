package com.byteforce.trailblaze.login

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.byteforce.trailblaze.R


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstPersonalizeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        //inflate the layout
        val view = inflater.inflate(R.layout.fragment_first_personalize, container, false)

        view.findViewById<Button>(R.id.continuebtn1).setOnClickListener {
            //navigate to SecondFragment
            (activity as? PersonalizeActivity)?.loadFragment(SecondPersonalizeFragment())
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //find image by their IDs
        val natureImage = view.findViewById<ImageView>(R.id.nature)
        val breaksImage = view.findViewById<ImageView>(R.id.breaks)
        val uphillImage = view.findViewById<ImageView>(R.id.uphill)
        val paceImage = view.findViewById<ImageView>(R.id.pace)

        //set click listener for each image
        natureImage.setOnClickListener{toggleBackgroundColor(natureImage)}
        breaksImage.setOnClickListener{toggleBackgroundColor(breaksImage)}
        uphillImage.setOnClickListener{toggleBackgroundColor(uphillImage)}
        paceImage.setOnClickListener{toggleBackgroundColor(paceImage)}
    }

    private fun toggleBackgroundColor(imageView: ImageView) {
        //get the current background color
        val currentColor = (imageView.background as? ColorDrawable)?.color

        //define the green color
        val greenColor = ContextCompat.getColor(requireContext(), R.color.green)

        //toggle between green and null
        if (currentColor == greenColor) {
            imageView.background = null
        } else {
            //change to green
            imageView.setBackgroundColor(greenColor)
        }
    }
}