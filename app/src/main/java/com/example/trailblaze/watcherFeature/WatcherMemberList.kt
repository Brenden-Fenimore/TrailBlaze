package com.example.trailblaze.watcherFeature

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.R

class WatcherMemberList : AppCompatActivity() {

    private lateinit var watcherRecyclerView: RecyclerView
    private lateinit var watcherAdapter: WatcherMemberAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_watcher_member_list)

        watcherRecyclerView = findViewById(R.id.watcherMemberRecyclerView)

        // Sample list of watchers
        val watcherMembers = listOf(
            WatcherProfile.WatcherMember("John Doe", R.drawable.no_image_available, R.drawable.badge),
            WatcherProfile.WatcherMember("Jane Smith", R.drawable.no_image_available, R.drawable.badge)
        )

        // Initialize and set up the adapter
        watcherAdapter = WatcherMemberAdapter(watcherMembers) { selectedWatcher ->
            // Navigate to WatcherProfile
            val intent = Intent(this, WatcherProfile::class.java)
            intent.putExtra("WATCHER_NAME", selectedWatcher.watcherName)
            startActivity(intent)
        }

        watcherRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@WatcherMemberList)
            adapter = watcherAdapter
        }
    }
}
