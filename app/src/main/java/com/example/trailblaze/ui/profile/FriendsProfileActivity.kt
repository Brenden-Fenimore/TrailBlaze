package com.example.trailblaze.ui.profile

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.R
import com.example.trailblaze.databinding.ActivityFriendsProfileBinding
import com.example.trailblaze.ui.achievements.AchievementManager
import com.example.trailblaze.ui.achievements.Badge
import com.example.trailblaze.ui.achievements.BadgesAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.trailblaze.firestore.ImageLoader

class FriendsProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFriendsProfileBinding
    private lateinit var achievementManager: AchievementManager
    private lateinit var badgesList: RecyclerView
    private lateinit var badgesAdapter: BadgesAdapter

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var userId: String

    // Define all possible badges
    private val allBadges = listOf(
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


        binding = ActivityFriendsProfileBinding.inflate(layoutInflater)
        setContentView(binding.root) // Set the content view here

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Get the user ID from the intent extras
        userId = intent.getStringExtra("friendUserId") ?: return

        // Set up the back button listener
        binding.chevronLeft.setOnClickListener {
            onBackPressed() // or finish() to close this activity
        }
        loadFriendProfile()
    }

    private fun loadFriendProfile() {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val username = document.getString("username")
                    val imageUrl = document.getString("profileImageUrl") // Adjust based on your Firestore structure

                    binding.username.text = username
                    // Load the profile picture using your ImageLoader utility
                    ImageLoader.loadProfilePicture(this, binding.profilePicture, imageUrl)

                    // Fetch and display badges (similar to your own profile)
                    val badges = document.get("badges") as? List<String> ?: emptyList()
                    updateBadgesList(badges)
                } else {
                    Log.e("FriendsProfileActivity", "Friend document does not exist")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FriendsProfileActivity", "Error fetching friend profile: ", exception)
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

            binding.badgesRecyclerView.adapter = badgesAdapter
        } else {
            badgesAdapter.updateBadges(unlockedBadges)
        }
    }
}