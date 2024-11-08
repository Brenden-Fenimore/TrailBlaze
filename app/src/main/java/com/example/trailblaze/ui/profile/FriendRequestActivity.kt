package com.example.trailblaze.ui.profile

import android.os.Bundle
import android.util.Log
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

    private lateinit var pendingRequestsRecyclerView: RecyclerView
    private lateinit var pendingRequestsAdapter: PendingRequestsAdapter
    private val pendingRequests = mutableListOf<PendingRequest>()
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_request) // Set your layout file here

        // Initialize RecyclerView
        pendingRequestsRecyclerView = findViewById(R.id.pendingRequestsRecyclerView)
        pendingRequestsRecyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize Adapter
        pendingRequestsAdapter = PendingRequestsAdapter(
            pendingRequests,
            onAcceptClicked = { userId -> acceptFriendRequest(userId) },
            onDeclineClicked = { userId -> declineFriendRequest(userId) }
        )
        pendingRequestsRecyclerView.adapter = pendingRequestsAdapter

        // Fetch pending requests data
        fetchPendingRequests()
    }

    private fun fetchPendingRequests() {
        val currentUserId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(currentUserId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val pendingRequestsList = document.get("pendingRequests") as? List<String> ?: emptyList()
                    pendingRequests.clear()
                    pendingRequests.addAll(pendingRequestsList.map { userId -> PendingRequest(userId, "Username") })
                    pendingRequestsAdapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FriendRequestActivity", "Error fetching pending requests", exception)
            }
    }

    private fun acceptFriendRequest(userId: String) {
        val currentUserId = auth.currentUser?.uid ?: return

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

    private fun declineFriendRequest(userId: String) {
        val currentUserId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(currentUserId)
            .update("pendingRequests", FieldValue.arrayRemove(userId))
            .addOnSuccessListener {
                pendingRequests.removeAll { it.userId == userId }
                pendingRequestsAdapter.notifyDataSetChanged()
                Toast.makeText(this, "Friend request declined", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Log.e("FriendRequestActivity", "Error declining friend request", exception)
            }
    }
}





