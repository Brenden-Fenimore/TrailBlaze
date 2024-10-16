package com.example.trailblaze.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.trailblaze.R
import com.example.trailblaze.databinding.FragmentEditProfileBinding
import android.widget.TextView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.EditText
import android.widget.Spinner
import android.widget.Switch
import android.widget.SeekBar

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        // Initialize views using ViewBinding
        binding.backButton.setOnClickListener {
            // Navigate to the profile fragment
            findNavController().navigate(R.id.action_editProfileFragment_to_profileFragment)
        }


        // Set up SeekBar range and listener
        binding.suggestedTrailsSeekbar.max = 200
        binding.suggestedTrailsSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Update the TextView to show current progress
                binding.suggestedTrailsValue.text = "$progress miles"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Optional: Implement if needed
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Optional: Implement if needed
            }
        })

        return view
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}