package com.example.trailblaze.ui.profile

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.R
import com.example.trailblaze.databinding.FragmentProfileBinding
import com.example.trailblaze.firestore.UserRepository
import com.example.trailblaze.ui.achievements.AchievementManager
import com.example.trailblaze.ui.achievements.BadgesAdapter
import com.example.trailblaze.ui.achievements.Badge
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.trailblaze.firestore.ImageLoader

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var userRepository: UserRepository

    private lateinit var achievementManager: AchievementManager
    private lateinit var badgesList: RecyclerView
    private lateinit var badgesAdapter: BadgesAdapter

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

        binding.editbutton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }

        //set click listener
        binding.chevronLeft.setOnClickListener {
            findNavController().navigateUp()
        }

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
            userRepository = UserRepository(firestore)
            loadProfilePicture()

        // Username fetching logic
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            fetchUsername()
            fetchUserBadges()
        } else {
            binding.username.text = "Not logged in"
        }

        achievementManager = AchievementManager(requireContext())
        badgesList = binding.badgesRecyclerView
        badgesList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

            // Initialize the icons
            binding.iconLocation.setOnClickListener {
                fetchCurrentUserLocation()
            }

            binding.iconDifficulty.setOnClickListener {
                fetchCurrentUserDifficulty()
            }

        return binding.root
    }

    private fun fetchUsername() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid // Get the current user's ID

        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val username = document.getString("username") // Fetch the username
                        binding.username.text = username // Set the username in the TextView
                    } else {
                        // Handle the case where the document does not exist
                        binding.username.text = "Username not found"
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle any errors
                }
        } else {
            // Handle the case where userId is null (not logged in)
            binding.username.text = "<UserName>"
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

    // Fetches users current location from Firestore
    private fun fetchCurrentUserLocation() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val location = document.getString("location") ?: "Location not found"
                    showMessage("Location", location)
                } else {
                    showMessage("Location", "Location not found")
                }
            }
            .addOnFailureListener {
                showMessage("Location", "Error fetching location")
            }
    }

    // Fetches users current Difficulty from Firestore
    private fun fetchCurrentUserDifficulty() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val difficulty = document.getString("difficulty") ?: "Difficulty not found"
                    showMessage("Difficulty", difficulty)
                } else {
                    showMessage("Difficulty", "Difficulty not found")
                }
            }
            .addOnFailureListener {
                showMessage("Difficulty", "Error fetching difficulty")
            }
    }

    // Helper function to show a message
    private fun showMessage(title: String, message: String) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setTitle(title)
        dialogBuilder.setMessage(message)
        dialogBuilder.setPositiveButton("OK", null)
        dialogBuilder.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}