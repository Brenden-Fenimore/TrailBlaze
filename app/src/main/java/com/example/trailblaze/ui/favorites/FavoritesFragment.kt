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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.trailblaze.nps.RetrofitInstance
import com.example.trailblaze.nps.NPSResponse
import com.example.trailblaze.nps.ParksAdapter
import com.example.trailblaze.ui.profile.FriendAdapter
import com.example.trailblaze.ui.profile.FriendsProfileActivity
import com.example.trailblaze.ui.profile.Friends


class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var favoritesRecyclerView: RecyclerView
    private lateinit var favoritesAdapter: ParksAdapter
    private var favoriteParks: MutableList<Park> = mutableListOf() // Use a mutable list
    private lateinit var bucketListParksRecyclerView: RecyclerView
    private lateinit var bucketListParksAdapter: ParksAdapter
    private var bucketListParks: MutableList<Park> = mutableListOf() // Use a mutable list

    private lateinit var favoriteFriendsRecyclerView: RecyclerView
    private lateinit var favoriteFriendsAdapter: FriendAdapter
    private var favoriteFriends: MutableList<Friends> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)

        // Initialize Firestore and Auth instances
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize Favorite Parks RecyclerView
        favoritesRecyclerView = binding.favoritesRecyclerView
        favoritesRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        // Set up the adapter for favorite parks, with an empty list to start
        favoritesAdapter = ParksAdapter(emptyList()) { park ->
            // Handle click event to open ParkDetailActivity with the selected park's code
            val intent = Intent(context, ParkDetailActivity::class.java).apply {
                putExtra("PARK_CODE", park.parkCode)
            }
            startActivity(intent)
        }
        // Attach the adapter to the RecyclerView for favorite parks
        favoritesRecyclerView.adapter = favoritesAdapter

        // Initialize Bucket List Parks RecyclerView
        bucketListParksRecyclerView = binding.bucketListParksRecyclerView
        bucketListParksRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        // Set up the adapter for bucket list parks, starting with an empty list
        bucketListParksAdapter = ParksAdapter(emptyList()) { park ->
            // Handle click event to open ParkDetailActivity with the selected park's code
            val intent = Intent(context, ParkDetailActivity::class.java).apply {
                putExtra("PARK_CODE", park.parkCode)
            }
            startActivity(intent)
        }
        // Attach the adapter to the RecyclerView for bucket list parks
        bucketListParksRecyclerView.adapter = bucketListParksAdapter

        fetchFavoriteParks()        // Fetch and display favorite parks from Firestore
        fetchBucketListParks()      // Fetch and display bucket list parks from Firestore

        binding.menuButton.setOnClickListener {
            val intent = Intent(activity, MenuActivity::class.java)
            startActivity(intent)
        }

        // initialize favorite friends recyclerView
        favoriteFriendsRecyclerView = binding.favoriteFriendsRecyclerView
        favoriteFriendsRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        // Set up the adapter for favorite friends, with an empty list to start
        favoriteFriendsAdapter = FriendAdapter(emptyList()) { user ->
            // Handle click event to open FriendDetailActivity with the selected friend's ID
            val intent = Intent(context, FriendsProfileActivity::class.java)
            intent.putExtra("friendUserId", user.userId)
            startActivity(intent)
        }
        // Attach the adapter to the RecyclerView for favorite friends
        favoriteFriendsRecyclerView.adapter = favoriteFriendsAdapter

        fetchFavoriteParks() // Fetch and display favorite parks
        fetchBucketListParks() // Fetch and display bucket list parks
        fetchFavoriteFriends() // Fetch and display favorite friends


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
        // Clear the existing list to avoid duplicates
        favoriteParks.clear()

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

        var completedRequests = 0

        tasks.forEach { call ->
            call.enqueue(object : Callback<NPSResponse> {
                override fun onResponse(call: Call<NPSResponse>, response: Response<NPSResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val park = response.body()?.data?.firstOrNull()
                        park?.let {
                            // Check if the park is already in the list before adding
                            if (!favoriteParks.contains(it)) {
                                favoriteParks.add(it)
                            }
                        }
                    }
                    completedRequests++
                    if (completedRequests == parkCodes.size) {
                        updateParksRecyclerView(favoriteParks)
                    }
                }

                override fun onFailure(call: Call<NPSResponse>, t: Throwable) {
                    Log.e("FavoritesFragment", "Error fetching park details: ${t.message}")
                    completedRequests++
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

    // Function to fetch the list of bucket list parks for the current user from Firestore
    private fun fetchBucketListParks() {
        // Get the current user's unique ID
        val userId = auth.currentUser?.uid ?: return
        // Get the current user's unique ID
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                // Retrieve the bucket list park codes as a list of strings or default to an empty list
                val bucketListParkCodes = document.get("bucketListParks") as? List<String> ?: emptyList()
                // Call the function to fetch detailed information for each park
                fetchBucketListParksDetails(bucketListParkCodes)
            }
            .addOnFailureListener { exception ->
                // Log any error that occurs while retrieving the document
                Log.e("FavoritesFragment", "Failed to retrieve bucket list parks: ${exception.message}")
            }
    }

    // Function to fetch detailed information for each park in the bucket list using the NPS API
    private fun fetchBucketListParksDetails(parkCodes: List<String>) {
        val tasks = parkCodes.map { parkCode ->
            RetrofitInstance.api.getParkDetails(parkCode)
        }

        var completedRequests = 0
        tasks.forEach { call ->
            call.enqueue(object : Callback<NPSResponse> {
                override fun onResponse(call: Call<NPSResponse>, response: Response<NPSResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val park = response.body()?.data?.firstOrNull()
                        park?.let {
                            // Check if the park is already in the list before adding
                            if (!bucketListParks.contains(it)) {
                                bucketListParks.add(it)
                            }
                        }
                    }
                    completedRequests++
                    // If all requests are completed, update the adapter with the fetched parks
                    if (completedRequests == parkCodes.size) {
                        bucketListParksAdapter.updateData(bucketListParks)
                    }
                }

                override fun onFailure(call: Call<NPSResponse>, t: Throwable) {
                    Log.e("FavoritesFragment", "Error fetching bucket list park details: ${t.message}")
                    completedRequests++
                    // Update the adapter once all requests have completed, even if some failed
                    if (completedRequests == parkCodes.size) {
                        bucketListParksAdapter.updateData(bucketListParks)
                    }
                }
            })
        }
    }

    private fun fetchFavoriteFriends() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val favoriteFriendIds = document.get("favoriteFriends") as? List<String> ?: emptyList()
                    // Call the utility function to fetch friend details
                    FriendUtils.fetchFriendsDetails(favoriteFriendIds, firestore) { friends ->
                        updateFriendsRecyclerView(friends)
                    }
                } else {
                    Log.d("FavoritesFragment", "User document does not exist or is null.")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FavoritesFragment", "Failed to retrieve user document: ${exception.message}")
            }
    }

    private fun updateFriendsRecyclerView(friends: List<Friends>) {
        favoriteFriendsAdapter.updateData(friends) // Update the adapter with the fetched friends
    }



}