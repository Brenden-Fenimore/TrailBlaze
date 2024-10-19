package com.example.trailblaze.ui.achievements

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BadgesActivity : AppCompatActivity() {

    private lateinit var badgesRecyclerView: RecyclerView
    private lateinit var sash: FrameLayout
    private lateinit var badgesAdapter: BadgesAdapter
    private val badges: List<Badge> = listOf(
        Badge("safetyexpert", "Safety Expert", R.drawable.safetyexpert),
        Badge("trailblaze", "TrailBlazer", R.drawable.trailblaze_logo),
        Badge("mountainclimber", "MountainClimber", R.drawable.mountainclimber),
        Badge("trekker", "Trekker", R.drawable.trekker),
        Badge("hiker", "Hiker", R.drawable.hhiker),
        Badge("weekendwarrior", "Weekend Warrior", R.drawable.weekendwarrior),
        Badge("dailyadventurer", "Daily Adventurer", R.drawable.dailyadventurer),
        Badge("conqueror", "Conqueror", R.drawable.conqueror),
        Badge("explorer", "Explorer", R.drawable.explorer),
        Badge("trailmaster", "Trailmaster", R.drawable.trailmaster),
        Badge("socialbutterfly", "Socialbutterfly", R.drawable.socialbutterfly),
        Badge("teamplayer", "Teleamplayer", R.drawable.teamplayer),
        Badge("communitybuilder", "Community Builder", R.drawable.communitybuilder),
        Badge("wildlifewatcher", "Wildlifewatcher", R.drawable.wildlifewatcher),
        Badge("photographer", "Photographer", R.drawable.photographer),
        Badge("goalsetter", "Goalsetter", R.drawable.goalsetter),
        Badge("badgecollector", "Badge Collector", R.drawable.badgecollector),
        Badge("leaderboard", "Leaderboard", R.drawable.leaderboard),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_badges)

        findViewById<ImageButton>(R.id.chevron_left).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<Button>(R.id.save).setOnClickListener {
            saveBadgesState()
            Toast.makeText(this, "Badges saved!", Toast.LENGTH_SHORT).show()
        }

        badgesRecyclerView = findViewById(R.id.recycler_view_badges)
        sash = findViewById(R.id.sashLayout)

        badgesAdapter = BadgesAdapter(badges) { badge ->

        }

        badgesRecyclerView.adapter = badgesAdapter
        badgesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        sash.setOnDragListener { view, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> true
                DragEvent.ACTION_DRAG_ENTERED -> true
                DragEvent.ACTION_DRAG_LOCATION -> true
                DragEvent.ACTION_DRAG_EXITED -> true
                DragEvent.ACTION_DROP -> {
                    // Ensure that clipData is not null
                    val dragData = event.clipData
                    if (dragData != null && dragData.itemCount > 0) {
                        // Get the badge resource ID from the ClipData
                        val drawableResId = dragData.getItemAt(0).text.toString().toInt()

                        // Add the badge to the sash
                        addBadgeToSash(drawableResId, event.x, event.y) // Call to add badge directly
                    } else {
                        Log.e("BadgesActivity", "ClipData is null or empty")
                    }
                    true
                }
                DragEvent.ACTION_DRAG_ENDED -> true
                else -> false
            }
        }
    }

    override fun onPause() {
        super.onPause()
        saveBadgesState() // Save the state when the activity is paused
    }

    override fun onResume() {
        super.onResume()
        restoreBadgesState() // Restore the state when the activity is resumed
    }

    private fun addBadgeToSash(drawableResId: Int, x: Float, y: Float) {
        val badge = ImageView(this)
        badge.setImageResource(drawableResId)

        // Set the size of the badge (adjust as necessary)
        val badgeSize = 100 // You can define the size of the badge here
        val params = FrameLayout.LayoutParams(badgeSize, badgeSize)

        // Position the badge based on drop coordinates
        params.leftMargin = (x - badgeSize / 2).toInt() // Center the badge on the drop location
        params.topMargin = (y - badgeSize / 2).toInt() // Center the badge on the drop location

        // Set the tag to store the drawable resource ID
        badge.tag = drawableResId

        // Add the badge to the sash (FrameLayout)
        sash.addView(badge)
        badge.layoutParams = params // Set the layout parameters after adding
        badge.requestLayout() // Request layout update
        Log.d("BadgesActivity", "Added badge with resource ID: $drawableResId to sash at x: $x, y: $y")
    }

    private fun saveBadgesState() {
        val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val badgeList = mutableListOf<String>()

        for (i in 0 until sash.childCount) {
            val badge = sash.getChildAt(i) as ImageView
            val drawableResId = badge.tag as? Int // Use safe cast
            if (drawableResId != null) { // Check if drawableResId is not null
                val x = (badge.layoutParams as FrameLayout.LayoutParams).leftMargin
                val y = (badge.layoutParams as FrameLayout.LayoutParams).topMargin
                badgeList.add("$drawableResId,$x,$y")
            } else {
                Log.e("BadgesActivity", "Badge tag is null or not an Int for badge at index $i")
            }
        }

        editor.putString("badges", badgeList.joinToString(";"))
        editor.apply()
    }

    private fun restoreBadgesState() {
        val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val badgesString = sharedPreferences.getString("badges", null)

        badgesString?.let {
            val badges = it.split(";")
            for (badgeData in badges) {
                val parts = badgeData.split(",")
                if (parts.size == 3) {
                    val drawableResId = parts[0].toIntOrNull() // Use toIntOrNull to safely convert
                    val x = parts[1].toFloatOrNull()
                    val y = parts[2].toFloatOrNull()

                    if (drawableResId != null && x != null && y != null) {
                        addBadgeToSash(drawableResId, x + sash.paddingLeft, y + sash.paddingTop)
                    } else {
                        Log.e("BadgesActivity", "Invalid badge data: $badgeData")
                    }
                }
            }
        }
    }
}