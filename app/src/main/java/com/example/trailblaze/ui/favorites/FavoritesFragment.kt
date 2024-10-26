package com.example.trailblaze.ui.favorites

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.databinding.FragmentFavoritesBinding
import com.example.trailblaze.nps.Park
import com.example.trailblaze.settings.SettingsScreenActivity
import com.example.trailblaze.ui.MenuActivity
import com.example.trailblaze.ui.parks.ParkDetailActivity
import com.example.trailblaze.ui.parks.ThumbnailAdapter
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.trailblaze.nps.RetrofitInstance
import com.example.trailblaze.nps.NPSResponse
import com.example.trailblaze.nps.ParksAdapter


class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var favoritesRecyclerView: RecyclerView
    private lateinit var favoritesAdapter: ParksAdapter
    private var favoriteParks: MutableList<Park> = mutableListOf() // Use a mutable list

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)

        // Initialize Firestore and Auth instances
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        favoritesRecyclerView = binding.favoritesRecyclerView
        favoritesRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        favoritesAdapter = ParksAdapter(emptyList()) { park ->
            val intent = Intent(context, ParkDetailActivity::class.java).apply {
                putExtra("PARK_CODE", park.parkCode)
            }
            startActivity(intent)
        }
        favoritesRecyclerView.adapter = favoritesAdapter

        fetchFavoriteParks()  // Fetch and display the favorite parks

        binding.menuButton.setOnClickListener {
            val intent = Intent(activity, MenuActivity::class.java)
            startActivity(intent)
        }

        binding.settingsbtn.setOnClickListener {
            val intent = Intent(activity, SettingsScreenActivity::class.java)
            startActivity(intent)
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fetchFavoriteParks() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    Log.d("FavoritesFragment", "User document retrieved successfully.")
                    val favoriteParkCodes = document.get("favoriteParks") as? List<String> ?: emptyList()
                    Log.d("FavoritesFragment", "Favorite park codes: $favoriteParkCodes")

                    fetchParksDetails(favoriteParkCodes)
                } else {
                    Log.d("FavoritesFragment", "User document does not exist or is null.")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FavoritesFragment", "Failed to retrieve user document: ${exception.message}")
            }
    }

    private fun fetchParksDetails(parkCodes: List<String>) {
        val tasks = parkCodes.map { parkCode ->
            RetrofitInstance.api.getParkDetails(parkCode)
        }

        // Track the number of responses
        var completedRequests = 0

        tasks.forEach { call ->
            call.enqueue(object : Callback<NPSResponse> {
                override fun onResponse(call: Call<NPSResponse>, response: Response<NPSResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val park = response.body()?.data?.firstOrNull()
                        park?.let {
                            favoriteParks.add(it) // Add the park to the list
                        }
                    }
                    completedRequests++
                    // Check if all requests are completed
                    if (completedRequests == parkCodes.size) {
                        updateParksRecyclerView(favoriteParks)
                    }
                }

                override fun onFailure(call: Call<NPSResponse>, t: Throwable) {
                    Log.e("FavoritesFragment", "Error fetching park details: ${t.message}")
                    completedRequests++
                    // Check if all requests are completed, even on failure
                    if (completedRequests == parkCodes.size) {
                        updateParksRecyclerView(favoriteParks)
                    }
                }
            })
        }
    }

    private fun updateParksRecyclerView(parks: List<Park>) {
        favoritesAdapter.updateData(parks) // Update the adapter with the fetched parks
    }
}