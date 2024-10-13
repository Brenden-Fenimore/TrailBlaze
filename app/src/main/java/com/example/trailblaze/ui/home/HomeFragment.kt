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
    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView
    private lateinit var parksAdapter: ParksAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialize RecyclerView
        recyclerView = binding.recyclerView // Assuming your FragmentHomeBinding includes recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        parksAdapter = ParksAdapter(emptyList())
        recyclerView.adapter = parksAdapter

        // Fetch data
        fetchParksData()

        return root
    }

    private fun fetchParksData() {
        RetrofitInstance.api.getParks(10).enqueue(object : Callback<NPSResponse> {
            override fun onResponse(call: Call<NPSResponse>, response: Response<NPSResponse>) {
                if (response.isSuccessful) {
                    val parks = response.body()?.data
                    parks?.let {
                        parksAdapter.updateData(it)
                    }
                }
            }

            override fun onFailure(call: Call<NPSResponse>, t: Throwable) {
                // Handle failure
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}