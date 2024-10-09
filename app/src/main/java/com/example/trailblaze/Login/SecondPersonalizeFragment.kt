package com.example.trailblaze.Login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import com.example.trailblaze.R

class SecondPersonalizeFragment : Fragment() {

    //declaration
    lateinit var city : EditText
    lateinit var state: EditText
    lateinit var zip : EditText

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

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //intitalize views
        city=view.findViewById(R.id.etCity)
        state=view.findViewById(R.id.etState)
        zip=view.findViewById(R.id.etZip)
    }
}