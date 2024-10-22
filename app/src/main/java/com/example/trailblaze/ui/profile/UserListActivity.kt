package com.example.trailblaze.ui.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trailblaze.databinding.ActivityUserListBinding
import com.google.firebase.firestore.FirebaseFirestore

class UserListActivity: AppCompatActivity(){
    private lateinit var binding: ActivityUserListBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var userAdapter: UserAdapter
    private lateinit var userList: List<User> // Replace User with your user data model class

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //set click listener
        binding.chevronLeft.setOnClickListener { onBackPressed() }

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Setup RecyclerView
        userAdapter = UserAdapter(emptyList()) { user ->
            // Handle user click
            val intent = Intent(this, FriendsProfileActivity::class.java)
            intent.putExtra("friendUserId", user.userId)
        }
        binding.userRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.userRecyclerView.adapter = userAdapter

        // Fetch users from Firestore
        fetchUsers()
    }

    private fun fetchUsers() {
        firestore.collection("users").get()
            .addOnSuccessListener { documents ->
                userList = documents.mapNotNull { document ->
                    val userId = document.id
                    val username = document.getString("username")
                    val profileImageUrl = document.getString("profileImageUrl")
                    if (username != null) {
                        User(userId, username, profileImageUrl) // Replace with your User model constructor
                    } else {
                        null
                    }
                }
                userAdapter.updateUserList(userList) // Update the adapter with the fetched user list
            }
            .addOnFailureListener { exception ->
                Log.e("UserListActivity", "Error fetching users: ", exception)
            }
    }
}