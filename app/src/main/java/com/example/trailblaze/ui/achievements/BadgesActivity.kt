package com.example.trailblaze.ui.achievements

import android.graphics.Color
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
import nl.dionsegijn.konfetti.KonfettiView
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size


// Activity that displays the user's badges
class BadgesActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore // Firestore instance for database operations
    private lateinit var auth: FirebaseAuth // Firebase Authentication instance for user management
    private lateinit var badgesRecyclerView: RecyclerView // RecyclerView to display the list of badges
    private lateinit var sash: FrameLayout // FrameLayout for special UI elements, possibly a sash for displaying badges
    private lateinit var badgesAdapter: BadgesAdapter // Adapter to bind badge data to the RecyclerView
    private var userBadges: MutableList<Badge> = mutableListOf() // List to hold the badges owned by the user
    private lateinit var achievementManager: AchievementManager // Manager for handling achievement-related logic
    private val addedBadgeIds: MutableSet<String> = mutableSetOf() // Set to track unique badge IDs that have been added
    private val addedBadgeTypes: MutableSet<String> = mutableSetOf() // Set to track unique badge types that have been added

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
        // Hide the ActionBar
        supportActionBar?.hide()

        // Initialize Firestore and Auth
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        findViewById<ImageButton>(R.id.chevron_left).setOnClickListener {
            onBackPressed()
        }

        findViewById<Button>(R.id.save).setOnClickListener {
            Toast.makeText(this, "Badges saved successfully", Toast.LENGTH_SHORT).show()
            achievementManager.checkAndGrantGoalBadge()
            showConfetti()
        }

        badgesRecyclerView = findViewById(R.id.recycler_view_badges)
        sash = findViewById(R.id.sashLayout)

        badgesAdapter = BadgesAdapter(userBadges) { badge ->
            // Handle badge click if necessary
        }

        achievementManager = AchievementManager(this)
        badgesRecyclerView.adapter = badgesAdapter
        badgesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        sash.setOnDragListener { _, event ->
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
        fetchSashedBadges()
    }

    //fucntion to fetch the current users badges
    private fun fetchUserBadges() {
        // Fetching badges logic here
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val badges = document.get("badges") as? List<String> ?: emptyList()
                        updateBadgesList(badges)

                        achievementManager.checkAndGrantBadgeCollectorBadge(userId)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error fetching badges: ", e)
                }
        }
    }

    //fucntion to update the badges list
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

    //fucntion to save the badge to the firestore
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

    //function to add a badge to the sash
    private fun addBadgeToSash(drawableResId: Int, x: Float, y: Float) {
        // Get the resource name from the drawable resource ID
        val resourceName = resources.getResourceEntryName(drawableResId)

        // Check if the badge type is already placed on the sash
        if (addedBadgeTypes.contains(resourceName)) {
            Toast.makeText(this, "You can only place one $resourceName badge on your sash.", Toast.LENGTH_SHORT).show()
            return // Exit the function if the badge type already exists
        }

        val badge = ImageView(this)
        badge.setImageResource(drawableResId)

        // Set the size of the badge (adjust as necessary)
        val badgeSize = 100
        val params = FrameLayout.LayoutParams(badgeSize, badgeSize)

        // Position the badge based on drop coordinates
        params.leftMargin = (x - badgeSize / 2).toInt()
        params.topMargin = (y - badgeSize / 2).toInt()

        Log.d("BadgesActivity", "Adding badge with resource ID: $drawableResId at x: ${params.leftMargin}, y: ${params.topMargin}")

        // Generate a unique badge ID using current timestamp
        val uniqueBadgeId = "${resourceName}_${System.currentTimeMillis()}"

        // Save the badge to Firestore with the resource name instead of the drawableResId
        saveBadgeToFirestore(uniqueBadgeId, resourceName, params.leftMargin, params.topMargin)

        // Mark this badge type as added
        addedBadgeTypes.add(resourceName) // Add the badge type to the set

        // Set the tag to store the unique badge ID
        badge.tag = uniqueBadgeId

        badge.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
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
                    // Finalize the position of the badge on release
                    // No removal logic here, simply return true
                    true
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

    //function to fetch sashed badges for current user
    private fun fetchSashedBadges() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Get the sashed badges from Firestore
                        val sashedBadges = document.get("sashedBadges") as? List<Map<String, Any>> ?: emptyList()
                        displaySashedBadges(sashedBadges)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error fetching sashed badges: ", e)
                }
        }
    }

    //function to display sashed badges
    private fun displaySashedBadges(sashedBadges: List<Map<String, Any>>) {
        for (badgeData in sashedBadges) {
            val badgeId = badgeData["badgeId"] as? String ?: continue
            val resourceName = badgeData["resourceName"] as? String ?: continue
            val x = (badgeData["x"] as? Number)?.toFloat() ?: continue
            val y = (badgeData["y"] as? Number)?.toFloat() ?: continue

            // Get the drawable resource ID from the resource name
            val drawableResId = resources.getIdentifier(resourceName, "drawable", packageName)

            // Create a new ImageView for the badge
            val badge = ImageView(this)
            badge.setImageResource(drawableResId)

            // Set the size of the badge (adjust as necessary)
            val badgeSize = 100
            val params = FrameLayout.LayoutParams(badgeSize, badgeSize)

            // Position the badge based on the fetched coordinates
            params.leftMargin = (x - badgeSize / 2).toInt()
            params.topMargin = (y - badgeSize / 2).toInt()

            // Set the layout parameters and add the badge to the sash
            badge.layoutParams = params
            sash.addView(badge)

            // Set up the drag functionality for the badge
            badge.setOnTouchListener { view, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        view.performClick() // Important for accessibility
                        true
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
                        true // Indicate that we are handling the up event
                    }
                    else -> false // Indicate that we are not handling other events
                }
            }

            // Mark this badge type as added to prevent duplicates
            addedBadgeTypes.add(resourceName)
        }
    }

    private fun showConfetti() {
        // Get the KonfettiView from the layout
        val konfettiView = findViewById<KonfettiView>(R.id.konfettiView)

        // Set the view to visible
        konfettiView.visibility = View.VISIBLE

        // Show confetti
        konfettiView.build()
            .addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA, Color.CYAN)
            .setDirection(0.0, 359.0) // Allow confetti to fall in all directions
            .setSpeed(1f, 5f)
            .setTimeToLive(3000L) // Increase the time to live to allow for longer fall
            .addShapes(Shape.Circle)
            .addSizes(Size(8))
            // Set the position to emit from the right side and farther down
            .setPosition(konfettiView.width + 400f, konfettiView.width + 400f, -100f, -50f)
            .stream(300, 3000L) // Stream 300 particles for 3000 milliseconds (3 seconds)

        // Optionally hide the konfetti view after some time
        konfettiView.postDelayed({
            konfettiView.visibility = View.GONE
        }, 6000) // Hide after 6 seconds
    }

}