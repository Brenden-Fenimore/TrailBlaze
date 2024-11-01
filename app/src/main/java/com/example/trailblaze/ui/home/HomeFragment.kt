package com.example.trailblaze.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.databinding.FragmentHomeBinding
import com.example.trailblaze.firestore.UserManager
import com.example.trailblaze.nps.NPSResponse
import com.example.trailblaze.nps.Park
import com.example.trailblaze.nps.RetrofitInstance
import com.example.trailblaze.settings.SettingsScreenActivity
import com.example.trailblaze.ui.MenuActivity
import com.example.trailblaze.ui.parks.ParkDetailActivity
import com.example.trailblaze.ui.parks.ThumbnailAdapter
import com.example.trailblaze.ui.profile.FriendAdapter
import com.example.trailblaze.ui.profile.Friends
import com.example.trailblaze.ui.profile.FriendsProfileActivity
import com.example.trailblaze.ui.profile.UserListActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class HomeFragment : Fragment() {

    private lateinit var usersRecyclerView: RecyclerView
    private lateinit var usersAdapter: FriendAdapter
    private var friendsList: List<Friends> = listOf()

    private var _binding: FragmentHomeBinding? = null
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var parksList: List<Park> = listOf()
    private lateinit var parksRecyclerView: RecyclerView   // RecyclerView to display parks
    private lateinit var thumbnailAdapter: ThumbnailAdapter        // Adapter for RecyclerView

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        binding.menuButton.setOnClickListener {
            val intent = Intent(activity, MenuActivity::class.java)
            startActivity(intent)
        }

        binding.settingsbtn.setOnClickListener {
            val intent = Intent(activity, SettingsScreenActivity::class.java)
            startActivity(intent)
        }

        binding.addFriend.setOnClickListener {
            val intent = Intent(activity, UserListActivity::class.java)
            startActivity(intent)
        }

        // RecyclerView setup
        parksRecyclerView = binding.thumbnailRecyclerView
        parksRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        // Initialize the adapter with an empty list to start
        thumbnailAdapter = ThumbnailAdapter(emptyList(), emptyList()) { parkCode ->
            // Start the ParkDetailActivity with the park's parkCode
            val intent = Intent(context, ParkDetailActivity::class.java).apply {
                putExtra("PARK_CODE", parkCode) // Pass the park code directly
            }
            startActivity(intent)
        }

        // Set adapter to RecyclerView
        parksRecyclerView.adapter = thumbnailAdapter

        // Fetch parks data
        fetchParksByState(userState = "state")

        // Fetch parks data
        fetchParksData()

        // Username fetching logic
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            fetchUsername()
        } else {
            binding.homepageusername.text = "<UserName>"
        }

        // Initialize RecyclerView
        usersRecyclerView = binding.usersRecyclerView
        usersRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        // Set up the adapter
        usersAdapter = FriendAdapter(friendsList) { user ->
            val intent = Intent(context, FriendsProfileActivity::class.java)
            intent.putExtra("friendUserId", user.userId)
            startActivity(intent)
        }
        usersRecyclerView.adapter = usersAdapter

        // Load users (you would need to implement this)
        fetchUsers()
        fetchCurrentUser()
        fetchUserState()
        return root
    }

    private fun fetchUsername() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid // Get the current user's ID

        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val username = document.getString("username") // Fetch the username
                        binding.homepageusername.text = username // Set the username in the TextView
                    } else {
                        // Handle the case where the document does not exist
                        binding.homepageusername.text = "Username not found"
                    }
                }
                .addOnFailureListener {
                }
        } else {
            // Handle the case where userId is null (not logged in)
            binding.homepageusername.text = "Not logged in"
        }
    }

    private fun fetchParksData() {
        RetrofitInstance.api.getParks(10).enqueue(object : Callback<NPSResponse> {
            override fun onResponse(call: Call<NPSResponse>, response: Response<NPSResponse>) {
                if (response.isSuccessful) {
                    parksList = response.body()?.data ?: emptyList() // Save the parks list

                    // Log park codes in parksList for debugging
                    parksList.forEach { park ->
                        Log.d("HomeFragment", "Fetched park code: ${park.parkCode.trim()}") // Use trim to log
                    }

                    // Update the adapter with the park data
                    thumbnailAdapter.updateData(parksList)
                } else {
                    Log.e("HomeFragment", "Response not successful: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<NPSResponse>, t: Throwable) {
                Log.e("HomeFragment", "Error fetching parks: ${t.message}")
            }
        })
    }

    private fun fetchUsers() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid // Get the current user's ID

        firestore.collection("users").get()
            .addOnSuccessListener { documents ->
                friendsList = documents.mapNotNull { document ->
                    val userId = document.id
                    val username = document.getString("username")
                    val profileImageUrl = document.getString("profileImageUrl")

                    // Check for null username and ensure the user is not the current user
                    if (username != null && userId != currentUserId) {
                        Friends(userId, username, profileImageUrl) // Replace with your User model constructor
                    } else {
                        null
                    }
                }
                usersAdapter.updateUserList(friendsList) // Update the adapter with the fetched user list
            }
            .addOnFailureListener { exception ->
                Log.e("UserListActivity", "Error fetching users: ", exception)
            }
    }

    private fun fetchCurrentUser() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            UserManager.fetchUserData(userId, firestore) { user ->
                if (user != null) {
                } else {
                }
            }
        }
    }

    private fun fetchParksByState(userState: String) {
        RetrofitInstance.api.getParksbyQuery(userState).enqueue(object : Callback<NPSResponse> {
            override fun onResponse(call: Call<NPSResponse>, response: Response<NPSResponse>) {
                if (response.isSuccessful) {
                    parksList = response.body()?.data ?: emptyList()

                    // Map the list of parks to their thumbnail URLs and names
                    val parkData = parksList.map {
                        Pair(it.images.firstOrNull()?.url ?: "", it.fullName)
                    }

                    // Update the adapter with the park data (image URL + name)
                    thumbnailAdapter.updateData(parksList)
                } else {
                    Log.e("HomeFragment", "Response not successful: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<NPSResponse>, t: Throwable) {
                Log.e("HomeFragment", "Error fetching parks: ${t.message}")
            }
        })
    }

    private fun fetchUserState() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid // Get the current user's ID

        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val userState = document.getString("state") ?: "No State Found"// Fetch the username
                        fetchParksByState(userState)
                    }
                }
                .addOnFailureListener {
                }
        } else {
            // Handle the case where userId is null (not logged in)
            binding.homepageusername.text = "Not logged in"
        }
    }



    override fun onDestroyView()
    {
        super.onDestroyView()
        _binding = null
    }
}