package com.example.trailblaze.ui.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trailblaze.R
import com.example.trailblaze.databinding.FragmentProfileBinding
import com.example.trailblaze.firestore.UserRepository
import com.example.trailblaze.ui.achievements.AchievementManager
import com.example.trailblaze.ui.achievements.BadgesAdapter
import com.example.trailblaze.ui.achievements.Badge
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.trailblaze.firestore.ImageLoader
import com.example.trailblaze.firestore.UserManager
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var userRepository: UserRepository

    private lateinit var achievementManager: AchievementManager
    private lateinit var badgesList: RecyclerView
    private lateinit var badgesAdapter: BadgesAdapter

    private lateinit var userManager: UserManager

    private lateinit var friendAdapter: FriendAdapter
    private lateinit var friendsList: MutableList<Friends>

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialize the binding
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        // Initialize Firebase components
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        userRepository = UserRepository(firestore)

        // Initialize the RecyclerView
        friendsList = mutableListOf()

        friendAdapter = FriendAdapter(friendsList) { friend ->
            val intent = Intent(context, FriendsProfileActivity::class.java)
            intent.putExtra("friendUserId", friend.userId)
            startActivity(intent)
        }
        binding.friendsRecyclerView.adapter = friendAdapter


        // Initialize UserManager
        userManager = UserManager

        // Set up UI event listeners
        setupUIEventListeners()

        // Load current user's data
        loadCurrentUserData()

        // Initialize achievement manager and badges list
        achievementManager = AchievementManager(requireContext())
        badgesList = binding.badgesRecyclerView
        badgesList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        return binding.root
    }

    private fun setupUIEventListeners() {
        binding.editbutton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }

        // Set click listener for the back button
        binding.chevronLeft.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun loadCurrentUserData() {
        // Fetch the current user from UserManager
        val currentUser = userManager.getCurrentUser()

        if (currentUser != null) {
            // Set the username in the TextView
            binding.username.text = currentUser.username

            // Load the user's badges
            fetchUserBadges()

            // Load the user's profile picture
            loadProfilePicture()

            // Fetch friends when current user data is loaded
            fetchUserFriends()
        } else {
            binding.username.text = "Not logged in"
        }
    }

    private fun fetchUserFriends() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val friendsIds = document.get("friends") as? List<String> ?: emptyList()
                        loadFriendsData(friendsIds)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error fetching friends: ", e)
                }
        }
    }

    private fun loadFriendsData(friendIds: List<String>) {
        val tasks = mutableListOf<Task<DocumentSnapshot>>()

        for (friendId in friendIds) {
            tasks.add(firestore.collection("users").document(friendId).get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val friend = Friends(
                        userId  = friendId,
                        username = document.getString("username") ?: "Unknown",
                        profileImageUrl = document.getString("profileImageUrl")
                    )
                    friendsList.add(friend) // Add friend to the list
                }
            })
        }

        // Wait until all friend data is fetched
        Tasks.whenAllSuccess<DocumentSnapshot>(tasks).addOnSuccessListener {
            // Update the RecyclerView with the fetched friends
            friendAdapter.updateUserList(friendsList)
        }
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

            binding.badgesRecyclerView.adapter = badgesAdapter
        } else {
            badgesAdapter.updateBadges(unlockedBadges)
        }
    }

    private fun loadProfilePicture() {
        val userId = auth.currentUser?.uid ?: return
        userRepository.getUserProfileImage(userId) { imageUrl ->
            ImageLoader.loadProfilePicture(requireContext(), binding.profilePicture, imageUrl)
        }
    }
}