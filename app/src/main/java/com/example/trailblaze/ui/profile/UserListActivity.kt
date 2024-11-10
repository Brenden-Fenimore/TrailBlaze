package com.example.trailblaze.ui.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trailblaze.databinding.ActivityUserListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserListActivity: AppCompatActivity(){
    private lateinit var binding: ActivityUserListBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var friendAdapter: FriendAdapter
    private lateinit var friendsList: List<Friends> // Replace User with your user data model class

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        //set click listener
        binding.chevronLeft.setOnClickListener { onBackPressed() }

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Setup RecyclerView
        friendAdapter = FriendAdapter(emptyList()) { user ->
            // Handle user click
            val intent = Intent(this, FriendsProfileActivity::class.java)
            intent.putExtra("friendUserId", user.userId)
            startActivity(intent)
        }
        binding.userRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.userRecyclerView.adapter = friendAdapter

        // Fetch users from Firestore
        fetchUsers()
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
                        Friends(userId, username, profileImageUrl, isPrivateAccount,watcherVisible) // Replace with your User model constructor
                    } else {
                        null
                    }
                }
                friendAdapter.updateUserList(friendsList) // Update the adapter with the fetched user list
            }
            .addOnFailureListener { exception ->
                Log.e("UserListActivity", "Error fetching users: ", exception)
            }
    }
}