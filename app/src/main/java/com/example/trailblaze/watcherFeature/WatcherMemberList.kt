package com.example.trailblaze.watcherFeature

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.trailblaze.R
import android.content.Intent
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class WatcherMemberList : AppCompatActivity() {

    private lateinit var watcherProfileRecycler: RecyclerView
    private lateinit var watcherAdapter: WatcherAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_watcher_member_list)


        // Initialize the RecyclerView
        watcherProfileRecycler = findViewById(R.id.watcherMemberRecyclerView)

        // Set the layout manager
        watcherProfileRecycler.layoutManager = LinearLayoutManager(this)

        // Create sample data
        val watcherList = listOf(
            WatcherMember("John Doe", R.drawable.profile_no_image_available, R.drawable.badge),
            WatcherMember("Jane Smith", R.drawable.profile_no_image_available, R.drawable.badge),
            WatcherMember("Mike Johnson", R.drawable.profile_no_image_available, R.drawable.badge)
        )

        // Initialize the adapter and set it to the RecyclerView
        val adapter = WatcherAdapter(this, watcherList) { watcherMember ->
            val intent = Intent(this, WatcherProfile::class.java)
            startActivity(intent)
        }
        watcherProfileRecycler.adapter = adapter

        findViewById<ImageButton>(R.id.chevron_left).setOnClickListener {
            onBackPressed()
        }
    }
}