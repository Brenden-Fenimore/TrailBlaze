package com.example.trailblaze.features.achievements

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

        badgesRecyclerView = findViewById(R.id.recycler_view_badges)
        sash = findViewById(R.id.sash)

        badgesAdapter = BadgesAdapter(badges) { badge ->
            // Handle badge click to create and drag the badge
            createAndDragBadgeView(badge)
        }

        badgesRecyclerView.adapter = badgesAdapter
        badgesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        loadBadgePositions()

        // Fetch user badges from Firestore
        fetchUserBadges { userBadges ->
            // Update the badge list based on fetched user badges
            updateBadgesList(userBadges)
        }

        // Set the drag listener on the sash
        sash.setOnDragListener(sashDragListener)

    }

    private fun saveFrameLayoutState() {
        val sharedPreferences = getSharedPreferences("BadgeState", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear() // Clear previous saved state

        // Iterate through each child view in the sash
        for (i in 0 until sash.childCount) {
            val child = sash.getChildAt(i) as? ImageView ?: continue

            // Get badge ID from tag
            val badgeId = child.tag as? String ?: continue
            // Save position and visibility
            editor.putFloat("${badgeId}_x", child.x)
            editor.putFloat("${badgeId}_y", child.y)
            editor.putInt("${badgeId}_visibility", child.visibility)
        }

        editor.apply()
        Toast.makeText(this, "Badge layout saved!", Toast.LENGTH_SHORT).show()
    }


    private fun createAndDragBadgeView(badge: Badge) {
        // Create an ImageView for the clicked badge
        val badgeImageView = ImageView(this).apply {
            setImageResource(badge.resourceId)
            layoutParams = FrameLayout.LayoutParams(100, 100) // Set desired size
            tag = badge.id // Set tag to badge ID

            // Set OnTouchListener to handle dragging
            setOnTouchListener { view, motionEvent ->
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        val dragShadowBuilder = View.DragShadowBuilder(view)
                        view.startDragAndDrop(null, dragShadowBuilder, view, 0)
                        true
                    }
                    else -> false
                }
            }
        }
        // Add the new ImageView to the sash for visual feedback
        sash.addView(badgeImageView)

        badgeImageView.x = sash.width / 2f - badgeImageView.width / 2f
        badgeImageView.y = sash.height / 2f - badgeImageView.height / 2f
    }

    private val sashDragListener = View.OnDragListener { v, event ->
        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                Log.d("DragAndDrop", "Drag started")
                true
            }

            DragEvent.ACTION_DRAG_ENTERED -> {
                Log.d("DragAndDrop", "Drag entered")
                true
            }

            DragEvent.ACTION_DRAG_EXITED -> {
                Log.d("DragAndDrop", "Drag exited")
                true
            }

            DragEvent.ACTION_DROP -> {
                // Log the drop coordinates
                val dropX = event.x
                val dropY = event.y
                Log.d("DragAndDrop", "Drop coordinates: x = $dropX, y = $dropY")

                val draggedView = event.localState as? View

                // Check if draggedView is valid
                if (draggedView == null) {
                    Log.d("DragAndDrop", "Dragged view is null")
                    return@OnDragListener false
                }

                // Center the badge on drop
                val x = dropX - (draggedView.width / 2)
                val y = dropY - (draggedView.height / 2)

                // Remove the dragged view from its parent
                (draggedView.parent as? ViewGroup)?.removeView(draggedView)

                // Set layout parameters and position for the badge
                val params = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )
                draggedView.layoutParams = params
                draggedView.scaleX = 0.1f
                draggedView.scaleY = 0.1f
                draggedView.x = x
                draggedView.y = y
                draggedView.visibility = View.VISIBLE

                // Add the dragged badge to the sash
                (v as FrameLayout).addView(draggedView)

                // Save position if necessary
                val badgeId = draggedView.tag as? String ?: return@OnDragListener false
                saveBadgePosition(badgeId, x, y)

                Log.d("DragAndDrop", "Drop successful")
                true
            }

            DragEvent.ACTION_DRAG_ENDED -> {
                Log.d("DragAndDrop", "Drag ended")
                true
            }

            else -> false
        }
    }

    private fun saveBadgeVisibility(badgeId: String, visibility: Int) {
        val sharedPreferences = getSharedPreferences("BadgePositions", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("${badgeId}_visibility", visibility)
        editor.apply()
    }
    private fun saveBadgePosition(badgeId: String, x: Float, y: Float) {
        val sharedPreferences = getSharedPreferences("BadgePositions", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putFloat("${badgeId}_x", x)
        editor.putFloat("${badgeId}_y", y)
        editor.apply() // Save the changes

        Log.d("BadgesActivity", "Saved position for $badgeId: x = $x, y = $y")
    }


    private fun loadBadgePositions() {
        val sharedPreferences = getSharedPreferences("BadgeState", MODE_PRIVATE)

        // Assuming badges is a list of all available badges
        badges.forEach { badge ->
            val x = sharedPreferences.getFloat("${badge.id}_x", -1f)
            val y = sharedPreferences.getFloat("${badge.id}_y", -1f)
            val visibility = sharedPreferences.getInt("${badge.id}_visibility", View.VISIBLE)

            // Check if the badge was previously saved
            if (x != -1f && y != -1f) {
                val badgeImageView = createBadgeView(badge.id) // Create the ImageView
                badgeImageView.x = x
                badgeImageView.y = y
                badgeImageView.visibility = visibility // Restore visibility

                // Add the loaded badge ImageView to the FrameLayout (sash)
                sash.addView(badgeImageView)
            }
        }
    }


    private fun fetchUserBadges(onResult: (List<String>) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val badges = document.get("badges") as? List<String> ?: emptyList()
                        onResult(badges) // Return the fetched badges to the caller
                    } else {
                        onResult(emptyList())
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error fetching badges: ", e)
                    onResult(emptyList())
                }
        } else {
            Log.e("Firestore", "User not logged in")
            onResult(emptyList())
        }
    }

    private fun updateBadgesList(userBadgeIds: List<String>) {
        // Filter the list of all badges based on fetched user badge IDs
        val unlockedBadges = badges.filter { userBadgeIds.contains(it.id) }

        // Check if the list of unlocked badges is different from the current adapter's data
        if (badgesAdapter.currentBadges != unlockedBadges) {
            badgesAdapter.updateBadges(unlockedBadges)
            badgesAdapter.notifyDataSetChanged()
        }
    }

    private fun createBadgeView(badgeId: String): ImageView {
        // Create and return an ImageView for the given badge ID
        val badge = badges.find { it.id == badgeId } ?: return ImageView(this)
        return ImageView(this).apply {
            setImageResource(badge.resourceId)
            layoutParams = FrameLayout.LayoutParams(100, 100)
            tag = badge.id
        }
    }
}

