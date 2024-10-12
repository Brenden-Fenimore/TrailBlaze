package com.example.trailblaze.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.trailblaze.databinding.FragmentHomeBinding
import com.example.trailblaze.ui.MenuActivity

class HomeFragment : Fragment()
{

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root

    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        _binding = null
    }
}