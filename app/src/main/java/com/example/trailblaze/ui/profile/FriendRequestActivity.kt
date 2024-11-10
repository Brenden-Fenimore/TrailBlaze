package com.example.trailblaze.ui.profile

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.example.trailblaze.ui.profile.PendingRequest

class FriendRequestActivity : AppCompatActivity() {

    // RecyclerView and Adapter for displaying pending friend requests
    private lateinit var pendingRequestsRecyclerView: RecyclerView
    private lateinit var pendingRequestsAdapter: PendingRequestsAdapter
    private val pendingRequests = mutableListOf<PendingRequest>()    // List to store pending requests

    // Firebase Authentication and Firestore instances
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_request) // Set your layout file here
        // Hide the ActionBar
        supportActionBar?.hide()

        findViewById<ImageButton>(R.id.chevron_left).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Initialize RecyclerView for displaying pending requests
        pendingRequestsRecyclerView = findViewById(R.id.pendingRequestsRecyclerView)
        pendingRequestsRecyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize Adapter and set click listeners for accept and decline actions
        pendingRequestsAdapter = PendingRequestsAdapter(
            pendingRequests,
            onAcceptClicked = { userId -> acceptFriendRequest(userId) },
            onDeclineClicked = { userId -> declineFriendRequest(userId) }
        )
        pendingRequestsRecyclerView.adapter = pendingRequestsAdapter

        // Fetch and display the list of pending friend requests
        fetchPendingRequests()
    }

    // Fetches pending friend requests from Firestore for the current user
    private fun fetchPendingRequests() {
        val currentUserId = auth.currentUser?.uid ?: return // Get the current user's ID

        firestore.collection("users").document(currentUserId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Retrieve and populate the pending requests list
                    val pendingRequestsList = document.get("pendingRequests") as? List<String> ?: emptyList()

                    pendingRequests.clear() // Clear existing requests to prevent duplicates

                    // Fetch details for each pending request user
                    pendingRequestsList.forEach { userId ->
                        firestore.collection("users").document(userId).get()
                            .addOnSuccessListener { userDocument ->
                                val username = userDocument.getString("username") ?: "Unknown User"
                                val profileImageUrl = userDocument.getString("profileImageUrl") ?: ""

                                // Add to pending requests with both username and profile picture
                                pendingRequests.add(PendingRequest(userId, username, profileImageUrl))
                                pendingRequestsAdapter.notifyDataSetChanged() // Notify adapter of data change
                            }.addOnFailureListener { exception ->
                                Log.e("FriendRequestActivity", "Error fetching user details for $userId", exception)
                            }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FriendRequestActivity", "Error fetching pending requests", exception)
            }
    }

    // Accepts a friend request by moving the user ID from pending requests to friends list
    private fun acceptFriendRequest(userId: String) {
        val currentUserId = auth.currentUser?.uid ?: return     // Get the current user's ID

        firestore.runBatch { batch ->
            // Remove userId from pendingRequests in the current user's document
            batch.update(firestore.collection("users").document(currentUserId), "pendingRequests", FieldValue.arrayRemove(userId))

            // Add userId to friends in the current user's document
            batch.update(firestore.collection("users").document(currentUserId), "friends", FieldValue.arrayUnion(userId))

            // Add currentUserId to friends in the friend's document
            batch.update(firestore.collection("users").document(userId), "friends", FieldValue.arrayUnion(currentUserId))
        }.addOnSuccessListener {
            // Remove the user from the RecyclerView list and notify the adapter
            pendingRequests.removeAll { it.userId == userId }
            pendingRequestsAdapter.notifyDataSetChanged()
            Toast.makeText(this, "Friend request accepted", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { exception ->
            Log.e("FriendRequestActivity", "Error accepting friend request", exception)
        }
    }

    // Declines a friend request by removing the user ID from the pending requests list
    private fun declineFriendRequest(userId: String) {
        val currentUserId = auth.currentUser?.uid ?: return     // Get the current user's ID

        firestore.collection("users").document(currentUserId)
            .update("pendingRequests", FieldValue.arrayRemove(userId))      // Remove userId from pending requests
            .addOnSuccessListener {
                // Remove the declined user from the list and update the adapter
                pendingRequests.removeAll { it.userId == userId }
                pendingRequestsAdapter.notifyDataSetChanged()
                Toast.makeText(this, "Friend request declined", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Log.e("FriendRequestActivity", "Error declining friend request", exception)
            }
    }
}





