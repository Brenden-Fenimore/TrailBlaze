package com.example.trailblaze.watcherFeature


import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.trailblaze.R
import com.example.trailblaze.databinding.ActivityWatcherProfileBinding
import com.example.trailblaze.firestore.ImageLoader
import com.example.trailblaze.firestore.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.trailblaze.firestore.User


class WatcherProfile : AppCompatActivity() {

    private lateinit var binding: ActivityWatcherProfileBinding
    private var firestore = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth
    private lateinit var userRepository: UserRepository
    private lateinit var userId: String

    // WatcherBadges Adapter
    private lateinit var adapter: WatcherBadgeAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityWatcherProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize firebase
        auth = FirebaseAuth.getInstance()
        userRepository = UserRepository(firestore)
        firestore = FirebaseFirestore.getInstance()


        // Back button listener
        binding.chevronLeft.setOnClickListener {
            onBackPressed()
        }

        // fetch user
        userId = intent.getStringExtra("userId") ?: FirebaseAuth.getInstance().currentUser?.uid ?: ""

        if(userId.isNotEmpty()){
            loadUserProfile(userId)
        }

        setupBadgeTouchListeners()
    }


    private fun loadUserProfile(userId: String) {

        // get user data from firestore
        firestore.collection("users").document(userId).get()
        .addOnSuccessListener { document->
            if(document.exists()){

                // extract user data
                val username = document.getString("username")?: "Unknown User"
                val profileImageUrl = document.getString("profile_image")
                val watcherRank = document.get("watcherRank").toString()
                val totalTrailsWatched = document.get("total_trails_watched").toString()
                val averageResponseTime = document.get("average_response_time").toString()
                val followupRate = document.get("follow_uo_rate").toString()

                // Set TextViews
                binding.watcherName.text = username
                binding.watcherRank.text = watcherRank
                binding.totalTrailsWatched.text = totalTrailsWatched
                binding.averageResponseTime.text = averageResponseTime
                binding.followupRate.text = followupRate


                // Load Profile Picture
                profileImageUrl?.let {
                    Glide.with(this).load(it).placeholder(R.drawable.account_circle)
                        .into(binding.watcherProfilePicture)
                }?: binding.watcherProfilePicture.setImageResource(R.drawable.account_circle)

                fetchWatcherDetails(userId)

            }
        }
            .addOnFailureListener { exception ->}
    }


    private fun setupBadgeTouchListeners() {
        val badgeContainers = listOf(
            binding.watcherBadges,
            binding.watcherBadges1,
            binding.watcherBadges2,
            binding.watcherBadges3
        )

        badgeContainers.forEach { watcherBadge ->
            watcherBadge.setOnClickListener {}
        }
    }

//    private fun loadCurrentUserData() {
//        // Fetch the current user from UserManager
//        lateinit var userManager: UserManager
//       //  var watcherName: String
//        val currentUser = userManager.getCurrentUser()
//
//        if (currentUser != null) {
//            // Set the watcherName in the TextView
//            binding.watcherName.text = currentUser.username
//
//            // Load the user's profile picture
//            loadProfilePicture()
//
//            // Load the user's badges
//            fetchWatcherDetails(userId)
//
//
//        } else {
//            binding.watcherName.text = "Not logged in"
//        }
//    }

    private fun loadProfilePicture() {
        val userId = auth.currentUser?.uid ?: return
        userRepository.getUserProfileImage(userId) { imageUrl ->
            ImageLoader.loadProfilePicture(this, binding.watcherProfilePicture, imageUrl)
        }
    }

//    private fun fetchWatcherBadges() {
//        // Fetching badges logic here
//        val userId = FirebaseAuth.getInstance().currentUser?.uid
//        if (userId != null) {
//            firestore.collection("users").document(userId).get()
//                .addOnSuccessListener { document ->
//                    if (document.exists()) {
//                        // Retrieve the badges as a List<String>
//                        val watcherBadges = document.get("watcherBadges") as? List<String> ?: emptyList()
//                        Log.d("WatcherProfileActivity", "Fetched watcherBadges: $watcherBadges") // Log for debugging
//                    }
//                }
//                .addOnFailureListener { e ->
//                    Log.e("Firestore", "Error fetching badges: ", e)
//                }
//        }
//
//
//    }

    private fun fetchWatcherDetails(userId: String) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                val watcherDetails = user?.watcherDetails

                if (watcherDetails != null) {
                    // Use watcherDetails to update the UI
                    Log.d("WatcherDetails", "Rank: ${watcherDetails.rank}")
                    Log.d("WatcherDetails", "Stats: ${watcherDetails.stats}")
                    Log.d("WatcherDetails", "Awards: ${watcherDetails.awards}")
                } else {
                    Log.d("WatcherDetails", "No watcher details found for this user.")
                }
            }
            .addOnFailureListener { e ->
                Log.e("WatcherDetails", "Error fetching watcher details", e)
            }
    }
}