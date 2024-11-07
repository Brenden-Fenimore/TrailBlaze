package com.example.trailblaze.watcherFeature

import android.media.Image
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.trailblaze.R

class WatcherProfile : AppCompatActivity() {

    // Data class definition
    data class WatcherMember(
        val watcherName: String,
        val watcherProfileImage: Int,
        val watcherBadgeImage: Int
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_watcher_profile)

        val watcher = WatcherMember("John Doe", R.drawable.no_image_available, R.drawable.badge )
    }
}

