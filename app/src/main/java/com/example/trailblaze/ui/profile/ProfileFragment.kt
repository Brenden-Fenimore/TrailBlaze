package com.example.trailblaze.ui.profile

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.navigation.fragment.findNavController
import com.example.trailblaze.R
import com.example.trailblaze.databinding.FragmentProfileBinding
import com.example.trailblaze.settings.SettingsScreenActivity

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialize the binding
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        binding.editbutton.setOnClickListener {
            val intent = Intent(requireActivity(), EditProfileActivity::class.java)
            startActivity(intent)
        }

        //set click listener
        binding.chevronLeft.setOnClickListener {
            findNavController().navigateUp()
        }
        return binding.root
    }
    

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}