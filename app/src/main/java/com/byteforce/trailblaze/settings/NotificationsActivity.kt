package com.byteforce.trailblaze.settings

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import com.byteforce.trailblaze.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class NotificationsActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var notificationsSwitch: Switch
    private lateinit var watcherSwitch: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)
        supportActionBar?.hide()

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Initialize switches
        notificationsSwitch = findViewById(R.id.notifications_switch)
        watcherSwitch = findViewById(R.id.watcherSwitch)

        // Load all preferences
        loadAllPreferences()

        // Set switch listeners
        notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            updateNotificationPreference(isChecked)
        }

        watcherSwitch.setOnCheckedChangeListener { _, isChecked ->
            updateWatcherPreference(isChecked)
        }

        findViewById<ImageButton>(R.id.chevron_left).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadAllPreferences() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val receiveNotifications = document.getBoolean("receiveNotifications") ?: true
                    val watcherVisible = document.getBoolean("watcherVisible") ?: false

                    notificationsSwitch.isChecked = receiveNotifications
                    watcherSwitch.isChecked = watcherVisible
                }
            }
    }

    private fun updateNotificationPreference(enabled: Boolean) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .update("receiveNotifications", enabled)
            .addOnSuccessListener {
                Log.d("NotificationsActivity", "Notification preference updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e("NotificationsActivity", "Error updating notification preference", e)
            }
    }

    private fun updateWatcherPreference(enabled: Boolean) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .update("watcherVisible", enabled)
            .addOnSuccessListener {
                Log.d("NotificationsActivity", "Watcher preference updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e("NotificationsActivity", "Error updating watcher preference", e)
            }
    }
}