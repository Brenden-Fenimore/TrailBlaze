package com.example.trailblaze.watcherFeature

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.trailblaze.R
import android.content.Intent
import android.util.Log
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.ui.profile.FriendAdapter
import com.example.trailblaze.ui.profile.Friends
import com.example.trailblaze.ui.profile.FriendsProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WatcherMemberList : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var friendsList: List<Friends>
    private lateinit var friendAdapter: FriendAdapter
    private lateinit var watcherProfileRecyclerView: RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_watcher_member_list)

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Initialize the RecyclerView
        watcherProfileRecyclerView = findViewById(R.id.watcherMemberRecyclerView)

        // Set the layout manager for the RecyclerView
        watcherProfileRecyclerView.layoutManager = LinearLayoutManager(this)

        // Setup the adapter for the RecyclerView and provide an empty list initially
        friendAdapter = FriendAdapter(emptyList()) { user ->
            // Handle user click
            val intent = Intent(this, FriendsProfileActivity::class.java)
            intent.putExtra("friendUserId", user.userId)
            startActivity(intent)
        }
        // Set the adapter to the RecyclerView
        watcherProfileRecyclerView.adapter = friendAdapter

        // Fetch users from Firestore after setting up RecyclerView
        fetchUsers()

        findViewById<ImageButton>(R.id.chevron_left).setOnClickListener {
            onBackPressed()
        }
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

                    // Log the values fetched to check if they are retrieving correctly
                    Log.d("FetchUsers", "UserId: $userId, Username: $username, IsPrivateAccount: $isPrivateAccount, WatcherVisible: $watcherVisible")

                    // Check for null username and ensure the user is not the current user and is visible
                    if (username != null && userId != currentUserId && watcherVisible) {
                        Friends(userId, username, profileImageUrl, isPrivateAccount, watcherVisible)
                    } else {
                        null
                    }
                }
                // Update the adapter with the filtered user list
                friendAdapter.updateUserList(friendsList)
            }
            .addOnFailureListener { exception ->
                Log.e("UserListActivity", "Error fetching users: ", exception)
            }
    }
}