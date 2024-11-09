package com.example.trailblaze.ui.home

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trailblaze.R
import com.example.trailblaze.databinding.ActivityPendingNotificationsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PendingNotificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPendingNotificationsBinding // Assuming you are using View Binding
    private lateinit var adapter: PendingNotificationAdapter
    // Firebase Authentication and Firestore instances
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPendingNotificationsBinding.inflate(layoutInflater) // Inflate binding
        setContentView(binding.root)

        findViewById<ImageButton>(R.id.chevron_left).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Initialize the RecyclerView
        adapter = PendingNotificationAdapter()
        binding.notificationRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.notificationRecyclerView.adapter = adapter

        // Fetch notifications and set the data to the adapter
        fetchPendingNotifications()
    }

    private fun fetchPendingNotifications() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        firestore.collection("users").document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val notifications: List<String> = document.get("notifications") as? List<String> ?: emptyList()
                    adapter.setNotifications(notifications) // Update adapter with fetched notifications
                }
            }
            .addOnFailureListener { exception ->
                Log.e("PendingNotificationActivity", "Error fetching notifications", exception)
            }
    }
}