package com.example.trailblaze.Login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.trailblaze.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ThirdPersonalizeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ThirdPersonalizeFragment : Fragment() {

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            //inflate the layout
            val view = inflater.inflate(R.layout.fragment_third_personalize, container, false)
            view.findViewById<Button>(R.id.continuebtn3).setOnClickListener {
                //navigate to ThirdFragment
                (activity as? PersonalizeActivity)?.loadFragment(FourthPersonalizeFragment())
            }

            return view
        }
    }