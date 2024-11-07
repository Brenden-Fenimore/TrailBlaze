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

    // Function to fetch details for a list of parks based on their codes
    private fun fetchParksDetails(parkCodes: List<String>) {
        // Create a list of network requests (calls) to fetch park details for each park code
        val tasks = parkCodes.map { parkCode ->
            RetrofitInstance.api.getParkDetails(parkCode) // Call the API to get details for each park
        }

        // Variable to track the number of completed requests
        var completedRequests = 0

        // Iterate through each network call to enqueue the requests
        tasks.forEach { call ->
            // Enqueue the network call asynchronously
            call.enqueue(object : Callback<NPSResponse> {
                // Callback method for handling a successful response from the API
                override fun onResponse(call: Call<NPSResponse>, response: Response<NPSResponse>) {
                    // Check if the response is successful and contains data
                    if (response.isSuccessful && response.body() != null) {
                        // Get the first park from the response data
                        val park = response.body()?.data?.firstOrNull()
                        // If a park is found, add it to the favoriteParks list if not already present
                        park?.let {
                            // Check if the park is already in the list before adding
                            if (!favoriteParks.contains(it)) {
                                favoriteParks.add(it) // Add the park to the favorites list
                            }
                        }
                    }
                    // Increment the count of completed requests
                    completedRequests++
                    // Check if all requests have completed
                    if (completedRequests == parkCodes.size) {
                        // Update the RecyclerView with the favorite parks once all requests are complete
                        updateParksRecyclerView(favoriteParks)
                    }
                }

                // Callback method for handling a failed response from the API
                override fun onFailure(call: Call<NPSResponse>, t: Throwable) {
                    // Log an error message indicating the failure to fetch park details
                    Log.e("FavoritesFragment", "Error fetching park details: ${t.message}")
                    // Increment the count of completed requests even on failure
                    completedRequests++
                    // Check if all requests have completed
                    if (completedRequests == parkCodes.size) {
                        // Update the RecyclerView with the favorite parks even if some requests failed
                        updateParksRecyclerView(favoriteParks)
                    }
                }
            })
        }
    }

    // Function to update the RecyclerView with the list of favorite parks
    private fun updateParksRecyclerView(parks: List<Park>) {
        // Update the adapter with the new list of fetched parks
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

    // Function to fetch favorite friends for the currently logged-in user
    private fun fetchFavoriteFriends() {
        // Get the current user's ID from Firebase Authentication
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Access the Firestore database to get the user's document
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                // Check if the document exists and is not null
                if (document != null && document.exists()) {
                    // Retrieve the list of favorite friend IDs from the document
                    val favoriteFriendIds = document.get("favoriteFriends") as? List<String> ?: emptyList()

                    // Call a utility function to fetch the details of the friends using their IDs
                    FriendUtils.fetchFriendsDetails(favoriteFriendIds, firestore) { friends ->
                        // Update the RecyclerView with the list of fetched friends
                        updateFriendsRecyclerView(friends)
                    }
                } else {
                    // Log a message if the user document does not exist or is null
                    Log.d("FavoritesFragment", "User document does not exist or is null.")
                }
            }
            // Handle failures in retrieving the user document
            .addOnFailureListener { exception ->
                // Log an error message with the exception details
                Log.e("FavoritesFragment", "Failed to retrieve user document: ${exception.message}")
            }
    }

    // Function to update the RecyclerView with the list of friends
    private fun updateFriendsRecyclerView(friends: List<Friends>) {
        // Update the favorite friends adapter with the new list of friends
        favoriteFriendsAdapter.updateData(friends) // Update the adapter with the fetched friends
    }



}