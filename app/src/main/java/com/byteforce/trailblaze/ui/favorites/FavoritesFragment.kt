package com.byteforce.trailblaze.ui.favorites

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byteforce.trailblaze.R
import com.byteforce.trailblaze.databinding.FragmentFavoritesBinding
import com.byteforce.trailblaze.settings.SettingsScreenActivity
import com.byteforce.trailblaze.ui.MenuActivity
import com.byteforce.trailblaze.ui.parks.ParkDetailActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.byteforce.trailblaze.nps.RetrofitInstance
import com.byteforce.trailblaze.nps.NPSResponse
import com.byteforce.trailblaze.ui.Map.LocationItem
import com.byteforce.trailblaze.ui.profile.FavoritesAdapter
import com.byteforce.trailblaze.ui.profile.FriendAdapter
import com.byteforce.trailblaze.ui.profile.FriendsProfileActivity
import com.byteforce.trailblaze.ui.profile.Friends
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest


class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var favoritesRecyclerView: RecyclerView
    private lateinit var favoritesAdapter: FavoritesAdapter
    private lateinit var bucketListParksRecyclerView: RecyclerView
    private lateinit var bucketListAdapter: FavoritesAdapter
    private lateinit var favoriteFriendsRecyclerView: RecyclerView
    private lateinit var favoriteFriendsAdapter: FriendAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)

        // Initialize Firestore and Auth instances
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize RecyclerView
        favoritesRecyclerView = binding.favoritesRecyclerView
        favoritesRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        setupFavoritesRecyclerView()
        loadFavoriteParks()

        // Initialize Bucket List Parks RecyclerView
        bucketListParksRecyclerView = binding.bucketListParksRecyclerView
        bucketListParksRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        setupBucketListRecyclerView()
        loadBucketListParks()

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
        fetchFavoriteFriends() // Fetch and display favorite friends

        
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

    // Function to fetch favorite friends for the currently logged-in user
    private fun fetchFavoriteFriends() {
        // Get the current user's ID from Firebase Authentication
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Access the Firestore database to get the user's document
        firestore.collection("users").document(userId).get()

            .addOnSuccessListener { document ->

                val yourWatcherList = document.get("favoriteFriends") as? List<String> ?: emptyList()
                updateYourWatchersCount(yourWatcherList.size)

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

    //Function to set up Favorites RecyclerView for both parks and places
    private fun setupFavoritesRecyclerView() {
        favoritesAdapter = FavoritesAdapter(emptyList()) { item ->
            when (item) {
                is LocationItem.ParkItem -> {
                    val intent = Intent(context, ParkDetailActivity::class.java)
                    intent.putExtra("PARK_CODE", item.park.parkCode)
                    startActivity(intent)
                }
                is LocationItem.PlaceItem -> {
                    val intent = Intent(context, ParkDetailActivity::class.java)
                    intent.putExtra("PLACE_ID", item.place.id)
                    startActivity(intent)
                }
            }
        }
        favoritesRecyclerView.adapter = favoritesAdapter
    }

    //fucntion to load both parks and or places
    private fun loadFavoriteParks() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->

                val favoriteTrails = document.get("favoriteParks") as? List<String> ?: emptyList()
                updateFavoriteTrailsCount(favoriteTrails.size)

                if (document != null && document.exists()) {
                    val favoriteParksList = document.get("favoriteParks") as? List<String> ?: emptyList()
                    val locationItems = mutableListOf<LocationItem>()

                    // Separate park codes and place IDs
                    val parkCodes = favoriteParksList.filter { it.length < 10 }
                    val placeIds = favoriteParksList.filter { it.length >= 10 }

                    var totalLoaded = 0
                    val totalItems = parkCodes.size + placeIds.size

                    // Fetch parks from NPS API
                    parkCodes.forEach { parkCode ->
                        RetrofitInstance.api.getParkDetails(parkCode).enqueue(object : Callback<NPSResponse> {
                            override fun onResponse(call: Call<NPSResponse>, response: Response<NPSResponse>) {
                                response.body()?.data?.firstOrNull()?.let { park ->
                                    locationItems.add(LocationItem.ParkItem(park))
                                }
                                totalLoaded++
                                checkAndUpdateAdapter(totalLoaded, totalItems, locationItems)
                            }

                            override fun onFailure(call: Call<NPSResponse>, t: Throwable) {
                                totalLoaded++
                                checkAndUpdateAdapter(totalLoaded, totalItems, locationItems)
                            }
                        })
                    }

                    // Fetch places from Google Places API
                    val placeFields = listOf(
                        Place.Field.ID,
                        Place.Field.DISPLAY_NAME,
                        Place.Field.PHOTO_METADATAS,
                        Place.Field.LOCATION
                    )

                    placeIds.forEach { placeId ->
                        val request = FetchPlaceRequest.builder(placeId, placeFields).build()
                        context?.let { ctx ->
                            Places.createClient(ctx).fetchPlace(request)
                                .addOnSuccessListener { response ->
                                    locationItems.add(LocationItem.PlaceItem(response.place))
                                    totalLoaded++
                                    checkAndUpdateAdapter(totalLoaded, totalItems, locationItems)
                                }
                                .addOnFailureListener {
                                    totalLoaded++
                                    checkAndUpdateAdapter(totalLoaded, totalItems, locationItems)
                                }
                        }
                    }
                }
            }
    }

    private fun checkAndUpdateAdapter(loaded: Int, total: Int, items: List<LocationItem>) {
        if (loaded == total) {
            favoritesAdapter.updateData(items)
        }
    }

    //Function to set up BucketList RecyclerView for both parks and places
    private fun setupBucketListRecyclerView() {
        bucketListAdapter = FavoritesAdapter(emptyList()) { item ->
            when (item) {
                is LocationItem.ParkItem -> {
                    val intent = Intent(context, ParkDetailActivity::class.java)
                    intent.putExtra("PARK_CODE", item.park.parkCode)
                    startActivity(intent)
                }
                is LocationItem.PlaceItem -> {
                    val intent = Intent(context, ParkDetailActivity::class.java)
                    intent.putExtra("PLACE_ID", item.place.id)
                    startActivity(intent)
                }
            }
        }
        bucketListParksRecyclerView.adapter = bucketListAdapter
    }

    //Function to load both parks or places
    private fun loadBucketListParks() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->

                val bucketList = document.get("bucketListParks") as? List<String> ?: emptyList()
                updateBucketListTrailsCount(bucketList.size)

                if (document != null && document.exists()) {
                    val bucketListParksList = document.get("bucketListParks") as? List<String> ?: emptyList()
                    val locationItems = mutableListOf<LocationItem>()

                    val parkCodes = bucketListParksList.filter { it.length < 10 }
                    val placeIds = bucketListParksList.filter { it.length >= 10 }

                    var totalLoaded = 0
                    val totalItems = parkCodes.size + placeIds.size

                    parkCodes.forEach { parkCode ->
                        RetrofitInstance.api.getParkDetails(parkCode).enqueue(object : Callback<NPSResponse> {
                            override fun onResponse(call: Call<NPSResponse>, response: Response<NPSResponse>) {
                                response.body()?.data?.firstOrNull()?.let { park ->
                                    locationItems.add(LocationItem.ParkItem(park))
                                }
                                totalLoaded++
                                checkAndUpdateBucketListAdapter(totalLoaded, totalItems, locationItems)
                            }

                            override fun onFailure(call: Call<NPSResponse>, t: Throwable) {
                                totalLoaded++
                                checkAndUpdateBucketListAdapter(totalLoaded, totalItems, locationItems)
                            }
                        })
                    }

                    val placeFields = listOf(
                        Place.Field.ID,
                        Place.Field.DISPLAY_NAME,
                        Place.Field.PHOTO_METADATAS,
                        Place.Field.LOCATION
                    )

                    placeIds.forEach { placeId ->
                        val request = FetchPlaceRequest.builder(placeId, placeFields).build()
                        context?.let { ctx ->
                            Places.createClient(ctx).fetchPlace(request)
                                .addOnSuccessListener { response ->
                                    locationItems.add(LocationItem.PlaceItem(response.place))
                                    totalLoaded++
                                    checkAndUpdateBucketListAdapter(totalLoaded, totalItems, locationItems)
                                }
                                .addOnFailureListener {
                                    totalLoaded++
                                    checkAndUpdateBucketListAdapter(totalLoaded, totalItems, locationItems)
                                }
                        }
                    }
                }
            }
    }

    private fun checkAndUpdateBucketListAdapter(loaded: Int, total: Int, items: List<LocationItem>) {
        if (loaded == total) {
            bucketListAdapter.updateData(items)
        }
    }

    private fun updateFavoriteTrailsCount(count: Int) {
        val favoriteTrailsTitle = view?.findViewById<TextView>(R.id.favoriteTrailsHeader)
        favoriteTrailsTitle?.text = getString(R.string.userFavoriteTrails, count)
    }

    private fun updateBucketListTrailsCount(count: Int) {
        val bucketListTrailsTitle = view?.findViewById<TextView>(R.id.bucketListTrailsTitle)
        bucketListTrailsTitle?.text = getString(R.string.bucketListTrails, count)
    }

    private fun updateYourWatchersCount(count: Int) {
        val yourWatcherTitle = view?.findViewById<TextView>(R.id.favoriteFriendsTitle)
        yourWatcherTitle?.text = getString(R.string.favoriteFriends, count)
    }

}