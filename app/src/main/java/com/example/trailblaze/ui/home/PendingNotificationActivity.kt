package com.example.trailblaze.ui.home

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trailblaze.R
import com.example.trailblaze.databinding.ActivityPendingNotificationsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class PendingNotificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPendingNotificationsBinding
    private lateinit var adapter: PendingNotificationAdapter

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPendingNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        findViewById<ImageButton>(R.id.chevron_left).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        adapter = PendingNotificationAdapter { notification, isChecked ->
            if (isChecked) {
                moveNotificationToReviewed(notification)
            }
        }
        binding.notificationRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.notificationRecyclerView.adapter = adapter

        fetchPendingAndReviewedNotifications() // Updated method
    }

    private fun fetchPendingAndReviewedNotifications() {
        val currentUserId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val pendingNotifications: List<String> =
                        document.get("pendingNotifications") as? List<String> ?: emptyList()
                    val reviewedNotifications: List<String> =
                        document.get("reviewedNotifications") as? List<String> ?: emptyList()
                    adapter.setNotifications(pendingNotifications, reviewedNotifications)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("PendingNotificationActivity", "Error fetching notifications", exception)
            }
    }

    private fun moveNotificationToReviewed(notification: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        val userDocRef = firestore.collection("users").document(currentUserId)

        userDocRef.update(
            "pendingNotifications", FieldValue.arrayRemove(notification),
            "reviewedNotifications", FieldValue.arrayUnion(notification)
        ).addOnSuccessListener {
            Log.d("PendingNotificationActivity", "Notification moved to reviewed successfully")
        }.addOnFailureListener { e ->
            Log.e("PendingNotificationActivity", "Error moving notification", e)
        }
    }
}