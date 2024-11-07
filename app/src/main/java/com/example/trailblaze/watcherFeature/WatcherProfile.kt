package com.example.trailblaze.watcherFeature

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.trailblaze.R

// Data class definition
data class WatcherMember(
    val watcherName: String,
    val watcherProfileImage: Int,
    val watcherBadgeImage: Int
)

class WatcherProfile : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_watcher_profile)

        // Retrieve data from the Intent
        val watcherName = intent.getStringExtra("watcherName") ?: "Unknown Watcher"
        val watcherProfileImage = intent.getIntExtra("watcherProfileImage", R.drawable.no_image_available)
        val watcherBadgeImage = intent.getIntExtra("watcherBadgeImage", R.drawable.badge)

        // Find views in the layout
        val nameTextView: TextView = findViewById(R.id.watcherName)
        val profileImageView: ImageView = findViewById(R.id.watcherProfilePicture)
        val badgeImageView: ImageView = findViewById(R.id.watcherBadges)

      // Set data to views
        nameTextView.text = watcherName
        profileImageView.setImageResource(watcherProfileImage)
        badgeImageView.setImageResource(watcherBadgeImage)
    }
}

