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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class BadgesActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var badgesRecyclerView: RecyclerView
    private lateinit var sash: FrameLayout
    private lateinit var badgesAdapter: BadgesAdapter
    private var userBadges: MutableList<Badge> = mutableListOf()
    private lateinit var achievementManager: AchievementManager
    private val addedBadgeIds: MutableSet<String> = mutableSetOf()

    private val allBadges: List<Badge> = listOf(
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
        Badge("teamplayer", "Teamplayer", R.drawable.teamplayer),
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

        // Initialize Firestore and Auth
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        findViewById<ImageButton>(R.id.chevron_left).setOnClickListener {
            onBackPressed()
        }

        findViewById<Button>(R.id.save).setOnClickListener {
            Toast.makeText(this, "Badges saved successfully", Toast.LENGTH_SHORT).show()
        }

        badgesRecyclerView = findViewById(R.id.recycler_view_badges)
        sash = findViewById(R.id.sashLayout)

        badgesAdapter = BadgesAdapter(userBadges) { badge ->
            // Handle badge click if necessary
        }

        achievementManager = AchievementManager(this)
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

        // Fetch the user's unlocked badges from Firestore
        fetchUserBadges()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }

    private fun fetchUserBadges() {
        // Fetching badges logic here
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val badges = document.get("badges") as? List<String> ?: emptyList()
                        updateBadgesList(badges)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error fetching badges: ", e)
                }
        }
    }
    private fun updateBadgesList(badges: List<String>) {
        // Filter the list of all badges based on fetched user badges
        val unlockedBadges = allBadges.filter { badges.contains(it.id) }

        // Initialize or update the adapter
        if (!::badgesAdapter.isInitialized) {
            badgesAdapter = BadgesAdapter(unlockedBadges, itemClickListener = { badge ->
                // Handle badge click
            })

            badgesRecyclerView.adapter = badgesAdapter
        } else {
            badgesAdapter.updateBadges(unlockedBadges)
        }
    }


    private fun addBadgeToSash(drawableResId: Int, x: Float, y: Float) {
        val badge = ImageView(this)
        badge.setImageResource(drawableResId)

        // Set the size of the badge (adjust as necessary)
        val badgeSize = 100
        val params = FrameLayout.LayoutParams(badgeSize, badgeSize)

        // Position the badge based on drop coordinates
        params.leftMargin = (x - badgeSize / 2).toInt()
        params.topMargin = (y - badgeSize / 2).toInt()

        Log.d("BadgesActivity", "Adding badge with resource ID: $drawableResId at x: ${params.leftMargin}, y: ${params.topMargin}")

        // Get the resource name from the drawable resource ID
        val resourceName = resources.getResourceEntryName(drawableResId)

        // Generate a unique badge ID using current timestamp
        val uniqueBadgeId = "${resourceName}_${System.currentTimeMillis()}"

        // Save the badge to Firestore with the resource name instead of the drawableResId
        saveBadgeToFirestore(uniqueBadgeId, resourceName, params.leftMargin, params.topMargin)

        // Set the tag to store the unique badge ID
        badge.tag = uniqueBadgeId

        // Check if the badge has already been added
        if (!addedBadgeIds.contains(uniqueBadgeId)) {
            addedBadgeIds.add(uniqueBadgeId) // Mark the badge as added
            // Save the badge to Firestore
        } else {
            Log.d("BadgesActivity", "Badge already added: $uniqueBadgeId")
        }

        // Set up touch listener for dragging
        badge.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Capture the initial touch position
                    view.performClick() // Important for accessibility
                    true // Indicate that we are handling the touch event
                }
                MotionEvent.ACTION_MOVE -> {
                    // Update the badge position while dragging
                    // Get the parent view's location on the screen
                    val location = IntArray(2)
                    sash.getLocationOnScreen(location)

                    // Calculate the new margins based on the touch event
                    val layoutParams = view.layoutParams as FrameLayout.LayoutParams
                    layoutParams.leftMargin = (event.rawX - location[0] - badgeSize / 2).toInt()
                    layoutParams.topMargin = (event.rawY - location[1] - badgeSize / 2).toInt()
                    view.layoutParams = layoutParams
                    view.requestLayout()
                    true // Indicate that we are handling the move event
                }
                MotionEvent.ACTION_UP -> {
                    // Save the new coordinates when the drag is released
                    val layoutParams = view.layoutParams as FrameLayout.LayoutParams
                    val newX = layoutParams.leftMargin.toFloat()
                    val newY = layoutParams.topMargin.toFloat()

                    true // Indicate that we are handling the touch event
                }
                else -> false // Indicate that we are not handling other events
            }
        }

        // Add the badge to the sash (FrameLayout)
        sash.addView(badge)
        badge.layoutParams = params // Set the layout parameters after adding
        badge.requestLayout() // Request layout update
        Log.d("BadgesActivity", "Added badge with resource ID: $drawableResId to sash at x: $x, y: $y")
    }

    private fun saveBadgeToFirestore(badgeId: String, resourceName: String, x: Int, y: Int) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(userId)

            // Create a new SashedBadge instance
            val sashedBadge = SashedBadge(badgeId, resourceName, x.toFloat(), y.toFloat())

            // Update the user document with the new badge info
            userRef.update("sashedBadges", FieldValue.arrayUnion(sashedBadge))
                .addOnSuccessListener {
                    Log.d("Firebase", "SashedBadge saved with ID: $badgeId at coordinates ($x, $y)")
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase", "Error saving sashed badge: ", e)
                }
        }
    }
}