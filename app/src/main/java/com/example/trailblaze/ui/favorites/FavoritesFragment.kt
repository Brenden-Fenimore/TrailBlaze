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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var favoritesRecyclerView: RecyclerView
    private lateinit var favoritesAdapter: ThumbnailAdapter
    private var favoriteParks: List<Park> = emptyList()


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

        favoritesAdapter = ThumbnailAdapter(emptyList(), emptyList()) { parkCode ->
            val intent = Intent(context, ParkDetailActivity::class.java).apply {
                putExtra("PARK_CODE", parkCode)
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
                    // Log to verify document retrieval
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
        val parks = mutableListOf<Park>()
        for (code in parkCodes) {
            firestore.collection("parks").document(code).get()
                .addOnSuccessListener { parkDoc ->
                    // Log each park retrieval attempt
                    Log.d("FavoritesFragment", "Fetched park data for code: $code")

                    parkDoc.toObject(Park::class.java)?.let { park ->
                        parks.add(park)
                        Log.d("FavoritesFragment", "Added park: ${park.fullName}")
                    } ?: Log.d("FavoritesFragment", "No park data found for code: $code")

                    if (parks.size == parkCodes.size) {
                        val parkData = parks.map { park ->
                            Pair(park.images.firstOrNull()?.url ?: "", park.fullName)
                        }
                        favoritesAdapter.updateData(parks)  // Update adapter when all parks are fetched
                        Log.d("FavoritesFragment", "All favorite parks fetched and UI updated.")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("FavoritesFragment", "Error fetching park details for code $code: ${exception.message}")
                }
        }
    }
}
