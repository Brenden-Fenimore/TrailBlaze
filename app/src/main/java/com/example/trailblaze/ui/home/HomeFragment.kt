package com.example.trailblaze.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.R
import com.example.trailblaze.databinding.FragmentHomeBinding
import com.example.trailblaze.firestore.UserManager
import com.example.trailblaze.nps.NPSResponse
import com.example.trailblaze.nps.Park
import com.example.trailblaze.nps.RetrofitInstance
import com.example.trailblaze.settings.SettingsScreenActivity
import com.example.trailblaze.ui.MenuActivity
import com.example.trailblaze.ui.parks.ParkDetailActivity
import com.example.trailblaze.ui.parks.ThumbnailAdapter
import com.example.trailblaze.ui.parks.TimeRecordAdapter
import com.example.trailblaze.ui.parks.TimeRecord
import com.example.trailblaze.ui.profile.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import com.example.trailblaze.ui.profile.PendingRequest


class HomeFragment : Fragment() {

    private lateinit var usersRecyclerView: RecyclerView
    private lateinit var usersAdapter: FriendAdapter
    private var friendsList: List<Friends> = listOf()

    private var _binding: FragmentHomeBinding? = null
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var parksList: List<Park> = listOf()
    private lateinit var parksRecyclerView: RecyclerView           // RecyclerView to display parks
    private lateinit var thumbnailAdapter: ThumbnailAdapter        // Adapter for RecyclerView
    private lateinit var timeRecordsRecyclerView: RecyclerView
    private lateinit var timeRecordAdapter: TimeRecordAdapter

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var greetingTextView: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        greetingTextView = binding.homepagegreeting
        updateGreeting()

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

        // Set up the click listener for the pending requests button on the homepage
        binding.pendingRequestsButton.setOnClickListener {
            // Create an intent to navigate to FriendRequestActivity when the button is clicked
            val intent = Intent(activity, FriendRequestActivity::class.java)
            startActivity(intent)
        }

        binding.notification.setOnClickListener{
            val intent = Intent(context, PendingNotificationActivity::class.java)
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

        // Initialize RecyclerView for time records
        timeRecordsRecyclerView = binding.timeRecordsRecyclerView
        timeRecordsRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        // Initialize adapter with an empty list and set it on the RecyclerView
        timeRecordAdapter = TimeRecordAdapter(mutableListOf()) { timeRecord ->
            val intent = Intent(context, ParkDetailActivity::class.java).apply {
                if (timeRecord.place) {
                    putExtra("PLACE_ID", timeRecord.placeId)
                    putExtra("IS_PLACE", true)
                } else {
                    putExtra("PARK_CODE", timeRecord.parkCode)
                    putExtra("IS_PLACE", false)
                }
            }
            startActivity(intent)
        }
        setupTimeRecordAdapter()
        timeRecordsRecyclerView.adapter = timeRecordAdapter

        fetchPendingRequestsAndUpdateCounter()
        fetchNotificationsCounter()

        // Load users (you would need to implement this)
        fetchUsers()
        fetchCurrentUser()
        fetchUserState()
        fetchTimeRecords()
        return root
    }
    override fun onResume() {
        super.onResume()
        fetchUserState() // Fetch the user state to update parks
    }

    // Function to update the greeting message based on the current time of day
    private fun updateGreeting() {
        // Get an instance of the Calendar class to retrieve the current date and time
        val calendar = Calendar.getInstance()
        // Get the current hour of the day (0-23 format)
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)

        // Determine the appropriate greeting based on the current hour
        val greeting: String =
            when {
                // If the hour is between 5 (inclusive) and 11 (inclusive), set greeting to "Good Morning"
                hourOfDay in 5..11 -> "Good Morning"
                // If the hour is between 12 (inclusive) and 17 (inclusive), set greeting to "Good Afternoon"
                hourOfDay in 12..17 -> "Good Afternoon"
                // For all other hours (18-24), set greeting to "Good Evening"
                else -> "Good Evening"
            }

        // Update the text of the greetingTextView with the determined greeting
        greetingTextView.text = greeting
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
                    val isPrivateAccount = document.getBoolean("isPrivateAccount") ?: false
                    val watcherVisible = document.getBoolean("watcherVisible") ?: false

                    // Check for null username and ensure the user is not the current user
                    if (username != null && userId != currentUserId) {
                        Friends(userId, username, profileImageUrl, isPrivateAccount, watcherVisible) // Replace with your User model constructor
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
        RetrofitInstance.api.getParksbyState(userState).enqueue(object : Callback<NPSResponse> {
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
                        val userState = document.getString("state") ?: "No State Found" // Fetch the state
                        fetchParksByState(userState) // Fetch parks based on the state
                    }
                }
                .addOnFailureListener {
                    Log.e("HomeFragment", "Error fetching user state: ${it.message}")
                }
        } else {
            // Handle the case where userId is null (not logged in)
            binding.homepageusername.text = "Not logged in"
        }
    }

    private fun fetchTimeRecords() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val timeRecordsData = document.get("timeRecords") as? List<Map<String, Any>>
                    val timeRecords = timeRecordsData?.mapNotNull { record ->
                        TimeRecord(
                            parkName = record["parkName"] as? String ?: return@mapNotNull null,
                            elapsedTime = record["elapsedTime"] as? String ?: return@mapNotNull null,
                            imageUrl = record["imageUrl"] as? String,
                            parkCode = record["parkCode"] as? String ?: return@mapNotNull null,
                            timestamp = record["timestamp"] as? Long ?: return@mapNotNull null,
                            date = record["date"] as? String ?: return@mapNotNull null,
                            place = record["isPlace"] as? Boolean ?: false,
                            placeId = record["placeId"] as? String
                        )
                    } ?: emptyList()

                    timeRecordAdapter.updateData(timeRecords)
                }
            }
    }

    // Fetches the list of pending friend requests for the current user from Firestore,
    // then updates the pending requests counter displayed on the homepage.
    private fun fetchPendingRequestsAndUpdateCounter() {
        // Get the current user's ID; if it's null (not logged in), return early
        val currentUserId = auth.currentUser?.uid ?: return

        // Retrieve the user's document from Firestore to access pending requests
        firestore.collection("users").document(currentUserId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Get the pending requests list, or default to an empty list if not present
                    val pendingRequestsList = document.get("pendingRequests") as? List<String> ?: emptyList()
                    // Reference to the counter TextView element
                    val counterTextView = binding.pendingRequestsCounter
                    // If there are no pending requests, hide the counter badge
                    if (pendingRequestsList.isEmpty()) {
                        counterTextView.visibility = View.GONE
                    } else {
                        // Set the counter to the size of the pending requests list and make it visible
                        counterTextView.text = pendingRequestsList.size.toString()
                        counterTextView.visibility = View.VISIBLE
                    }
                }
            }
            .addOnFailureListener { exception ->
                // Log an error message if fetching the pending requests fails
                Log.e("HomeFragment", "Error fetching pending requests", exception)
            }
    }

    // Fetches the list of notifications for the current user from Firestore,
    // then updates the notification counter displayed on the homepage.
    private fun fetchNotificationsCounter() {
        val currentUserId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(currentUserId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Check if user wants to receive notifications
                    val receiveNotifications = document.getBoolean("receiveNotifications") ?: true

                    if (receiveNotifications) {
                        val notificationList = document.get("pendingNotifications") as? List<String> ?: emptyList()
                        val counterTextView = binding.notificationCounter

                        if (notificationList.isEmpty()) {
                            counterTextView.visibility = View.GONE
                        } else {
                            counterTextView.text = notificationList.size.toString()
                            counterTextView.visibility = View.VISIBLE
                        }
                    } else {
                        // Hide notification counter if notifications are disabled
                        binding.notificationCounter.visibility = View.GONE
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("HomeFragment", "Error fetching notifications", exception)
            }
    }

    private fun setupTimeRecordAdapter() {
        timeRecordAdapter = TimeRecordAdapter(mutableListOf()) { timeRecord ->
            val intent = Intent(context, ParkDetailActivity::class.java)

            // Check if the parkCode looks like a Place ID (starts with "ChIJ")
            if (timeRecord.parkCode.startsWith("ChIJ")) {
                intent.putExtra("PLACE_ID", timeRecord.parkCode)
                Log.d("TimeRecordClick", "Opening as Place with ID: ${timeRecord.parkCode}")
            } else {
                intent.putExtra("PARK_CODE", timeRecord.parkCode)
                Log.d("TimeRecordClick", "Opening as Park with code: ${timeRecord.parkCode}")
            }
            startActivity(intent)
        }
    }


    override fun onDestroyView()
    {
        super.onDestroyView()
        _binding = null
    }
}