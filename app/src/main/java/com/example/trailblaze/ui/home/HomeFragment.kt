package com.example.trailblaze.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.ParksAdapter
import com.example.trailblaze.RetrofitInstance
import com.example.trailblaze.NPSResponse
import com.example.trailblaze.databinding.FragmentHomeBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!      // Safe access to binding when it's non-null

    private lateinit var recyclerView: RecyclerView     // RecyclerView to display parks
    private lateinit var parksAdapter: ParksAdapter     // Adapter for RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)      // Inflate layout using ViewBinding
        val root: View = binding.root

        // Initialize RecyclerView
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())      // Set vertical layout
        parksAdapter = ParksAdapter(emptyList())                                // Initialize adapter with an empty list
        recyclerView.adapter = parksAdapter                                     // Set adapter to RecyclerView

        fetchParksData()                                                        // Fetch park data from API

        return root
    }

    // Function to fetch park data using Retrofit
    private fun fetchParksData() {
        RetrofitInstance.api.getParks(10).enqueue(object : Callback<NPSResponse> {
            override fun onResponse(call: Call<NPSResponse>, response: Response<NPSResponse>) {
                if (response.isSuccessful) {
                    val parks = response.body()?.data                           // Update the RecyclerView with fetched parks data
                    parks?.let {
                        parksAdapter.updateData(it)
                    }
                }
            }

            override fun onFailure(call: Call<NPSResponse>, t: Throwable) {
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null                                                         // Release binding when view is destroyed
    }
}