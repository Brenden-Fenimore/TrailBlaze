package com.example.trailblaze.ui.profile

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.R
import com.example.trailblaze.databinding.ActivityFriendsProfileBinding
import com.example.trailblaze.ui.achievements.AchievementManager
import com.example.trailblaze.ui.achievements.Badge
import com.example.trailblaze.ui.achievements.BadgesAdapter
import com.google.firebase.auth.FirebaseAuth
import com.example.trailblaze.firestore.ImageLoader
import com.example.trailblaze.nps.NPSResponse
import com.example.trailblaze.nps.Park
import com.example.trailblaze.nps.ParksAdapter
import com.example.trailblaze.nps.RetrofitInstance
import com.example.trailblaze.ui.parks.ParkDetailActivity
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import nl.dionsegijn.konfetti.KonfettiView
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.trailblaze.ui.parks.TimeRecordAdapter
import com.example.trailblaze.ui.parks.TimeRecord

class FriendsProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFriendsProfileBinding
    private lateinit var achievementManager: AchievementManager
    private lateinit var badgesList: RecyclerView
    private lateinit var badgesAdapter: BadgesAdapter

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var userId: String
    private lateinit var addFriendButton: ImageButton

    private lateinit var friendsInCommonRecyclerView: RecyclerView
    private lateinit var friendsInCommonAdapter: FriendAdapter
    private lateinit var friendsInCommonList: MutableList<Friends>
    private lateinit var removeFriendImageButton: ImageButton

    private lateinit var photosAdapter: PhotosAdapter
    private val photoUrls = mutableListOf<String>()

    private lateinit var favoritesRecyclerView: RecyclerView
    private lateinit var favoritesAdapter: ParksAdapter
    private var favoriteParks: MutableList<Park> = mutableListOf()

    private lateinit var timeRecordsRecyclerView: RecyclerView
    private lateinit var timeRecordAdapter: TimeRecordAdapter

    // Define all possible badges
    private val allBadges = listOf(
        Badge("safetyexpert", "Safety Expert", R.drawable.safetyexpert),
        Badge("trailblazer", "TrailBlazer", R.drawable.trailblaze_logo),
        Badge("mountainclimber", "MountainClimber", R.drawable.mountainclimber),
        Badge("longdistancetrekker", "Trekker", R.drawable.trekker),
        Badge("habitualhiker", "Hiker", R.drawable.hhiker),
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
        // Hide the ActionBar
        supportActionBar?.hide()

        binding = ActivityFriendsProfileBinding.inflate(layoutInflater)
        setContentView(binding.root) // Set the content view here

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize the achievement manager
        achievementManager = AchievementManager(this)

        binding.iconLocation.setOnClickListener {
            fetchCurrentUserLocation()
        }

        binding.iconDifficulty.setOnClickListener {
            fetchCurrentUserDifficulty()
        }


        // Initialize the RecyclerView for friends in common
        friendsInCommonList = mutableListOf()
        friendsInCommonAdapter = FriendAdapter(friendsInCommonList) { user ->
            val intent = Intent(this, FriendsProfileActivity::class.java)
            intent.putExtra("friendUserId", user.userId)
            startActivity(intent)
        }

        friendsInCommonRecyclerView = binding.friendsInCommonRecyclerView
        friendsInCommonRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        friendsInCommonRecyclerView.adapter = friendsInCommonAdapter

        badgesList = binding.badgesRecyclerView
        badgesList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        badgesAdapter = BadgesAdapter(emptyList()){badge->
        }
        badgesList.adapter = badgesAdapter

        // Get the user ID from the intent extras
        userId = intent.getStringExtra("friendUserId") ?: return

        // Set up the back button listener
        binding.chevronLeft.setOnClickListener {
            onBackPressed() // or finish() to close this activity
        }
        loadFriendProfile()
        fetchFriendsInCommon()
        loadFavoriteParks()
        // Fetch leaderboard data
        fetchLeaderboard()

        // Initialize RecyclerView and Adapter
        val recyclerView = findViewById<RecyclerView>(R.id.photosRecyclerView)
        photosAdapter = PhotosAdapter(photoUrls, isOwnProfile = false)
        recyclerView.adapter = photosAdapter
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Initialize the "Add" button
        addFriendButton = binding.addFriendButton

        addFriendButton.setOnClickListener {
            addFriend(userId) // Call the function to add friend
        }

        // Initialize the remove friend button
        removeFriendImageButton = binding.removeFriendButton
        removeFriendImageButton.setOnClickListener {
            confirmRemoveFriend(userId) // Prompt for confirmation before removal
        }

        // Check if the user is already a friend and update the UI accordingly
        val currentUserId = auth.currentUser?.uid ?: return
        checkFriendshipStatus(currentUserId, userId)

        favoritesRecyclerView = binding.favoriteTrailsSection
        favoritesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        favoritesAdapter = ParksAdapter(emptyList()) { park ->
            val intent = Intent(this, ParkDetailActivity::class.java).apply {
                putExtra("PARK_CODE", park.parkCode)
            }
            startActivity(intent)
        }
        favoritesRecyclerView.adapter = favoritesAdapter

        // Initialize RecyclerView for time records
        timeRecordsRecyclerView = binding.timeRecordsRecyclerView
        timeRecordsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Initialize adapter with an empty list and set it on the RecyclerView
        timeRecordAdapter = TimeRecordAdapter(mutableListOf()) { timeRecord ->
            // Create an intent to start the ParkDetailActivity
            val intent = Intent(this, ParkDetailActivity::class.java).apply {
                putExtra("PARK_CODE", timeRecord.parkCode) // Pass the park code
            }
            startActivity(intent) // Start the ParkDetailActivity
        }
        timeRecordsRecyclerView.adapter = timeRecordAdapter
    }

    private fun loadFriendProfile() {
        val currentUserId = auth.currentUser?.uid ?: return // Get the current user's ID

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val username = document.getString("username")
                    val imageUrl = document.getString("profileImageUrl")
                    val isPrivateAccount = document.getBoolean("isPrivateAccount") ?: false

                    binding.username.text = username
                    ImageLoader.loadProfilePicture(this, binding.profilePicture, imageUrl)

                    // Retrieve visibility settings
                    val isLeaderboardVisible = document.getBoolean("leaderboardVisible") ?: true
                    val isPhotosVisible = document.getBoolean("photosVisible") ?: true
                    val isFavoriteTrailsVisible = document.getBoolean("favoriteTrailsVisible") ?: true
                    val watcherVisible = document.getBoolean("watcherVisible") ?: false

                    // Set visibility based on the privacy settings
                    binding.leaderRecyclerView.visibility = if (isLeaderboardVisible) View.VISIBLE else View.GONE
                    binding.leaderboardHeader.visibility = if (isLeaderboardVisible) View.VISIBLE else View.GONE

                    binding.photosRecyclerView.visibility = if (isPhotosVisible) View.VISIBLE else View.GONE
                    binding.photosHeader.visibility = if (isPhotosVisible) View.VISIBLE else View.GONE

                    binding.favoriteTrailsSection.visibility = if (isFavoriteTrailsVisible) View.VISIBLE else View.GONE
                    binding.favoriteTrailsHeader.visibility = if (isFavoriteTrailsVisible) View.VISIBLE else View.GONE

                    // Set visibility for the watcherMember TextView
                    binding.watcherMember.visibility = if (watcherVisible) View.VISIBLE else View.GONE

                    // Check if the account is private
                    if (isPrivateAccount) {
                        // Hide all user information
                        hideUserInformation()
                    } else {
                        // Load and display other information
                        loadUserOtherInformation(document) // Ensure this method is called only if the account isn't private
                    }

                    // Check friendship status and update UI
                    checkFriendshipStatus(currentUserId, userId)

                    // Fetch and display badges
                    val badges = document.get("badges") as? List<String> ?: emptyList()
                    updateBadgesList(badges)

                    // If the account is not private, fetch friend photos and time records
                    if (!isPrivateAccount) {
                        fetchFriendPhotos(userId)
                        fetchTimeRecordsForFriend(userId)
                    }
                } else {
                    Log.e("FriendsProfileActivity", "Friend document does not exist")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FriendsProfileActivity", "Error fetching friend profile: ", exception)
            }
    }
    private fun hideUserInformation() {
        // Hide sections that should not be visible for a private account
        binding.leaderRecyclerView.visibility = View.GONE
        binding.favoriteTrailsSection.visibility = View.GONE
        binding.photosRecyclerView.visibility = View.GONE
        binding.badgesRecyclerView.visibility = View.GONE
        binding.favoriteTrailsHeader.visibility = View.GONE
        binding.photosHeader.visibility = View.GONE
        binding.leaderboardHeader.visibility = View.GONE
        binding.completedParksHeader.visibility = View.GONE
        binding.timeRecordsRecyclerView.visibility = View.GONE
        binding.watcherMember.visibility = View.GONE


    }

    private fun loadUserOtherInformation(document: DocumentSnapshot) {
        // Retrieve visibility settings from the document
        val isLeaderboardVisible = document.getBoolean("leaderboardVisible") ?: true
        val isPhotosVisible = document.getBoolean("photosVisible") ?: true
        val isFavoriteTrailsVisible = document.getBoolean("favoriteTrailsVisible") ?: true

        // Set visibility based on the privacy settings
        binding.leaderRecyclerView.visibility = if (isLeaderboardVisible) View.VISIBLE else View.GONE
        binding.favoriteTrailsSection.visibility = if (isFavoriteTrailsVisible) View.VISIBLE else View.GONE
        binding.photosRecyclerView.visibility = if (isPhotosVisible) View.VISIBLE else View.GONE

        // Optionally load other information like badges or favorite parks
        val badges = document.get("badges") as? List<String> ?: emptyList()
        updateBadgesList(badges)

        // Load favorite parks or any other relevant information
        loadFavoriteParks()
    }

    private fun checkFriendshipStatus(currentUserId: String, friendId: String) {
        firestore.collection("users").document(currentUserId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val friendsList = document.get("friends") as? List<String> ?: emptyList()
                    val favoriteFriendsList = document.get("favoriteFriends") as? List<String> ?: emptyList()

                    // Check if the friend is already in the friends list
                    if (friendsList.contains(friendId)) {
                        binding.addFriendButton.visibility = View.GONE // Hide add friend button
                        binding.favoriteFriendBtn.visibility = View.VISIBLE // Show favorite button
                        binding.removeFriendButton.visibility = View.VISIBLE // Show unfriend button
                        // Check if the friend is in favorites
                        if (favoriteFriendsList.contains(friendId)) {
                            binding.favoriteFriendBtn.setImageResource(R.drawable.favorite_filled) // Set filled icon
                        } else {
                            binding.favoriteFriendBtn.setImageResource(R.drawable.favorite) // Set outline icon
                        }
                    } else {
                        binding.addFriendButton.visibility = View.VISIBLE // Show add friend button
                        binding.favoriteFriendBtn.visibility = View.GONE // Hide favorite button
                        binding.removeFriendButton.visibility = View.GONE   // Hide unfriend button
                    }

                    // Set up click listener for the favorite button
                    binding.favoriteFriendBtn.setOnClickListener {
                       toggleFavoriteStatus(currentUserId, friendId)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FriendsProfileActivity", "Error fetching user friends: ", exception)
            }
    }

    // Displays a confirmation dialog for removing a friend with the friend's username
    private fun confirmRemoveFriend(friendId: String) {
        // Fetch friend's username
        firestore.collection("users").document(friendId).get()
            .addOnSuccessListener { document ->
                val friendUsername = document.getString("username") ?: "this friend"    // Use "this friend" if username is missing

                // Show the confirmation dialog with the friend's username
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Remove Friend")
                builder.setMessage("Are you sure you want to remove $friendUsername?")
                builder.setPositiveButton("Yes") { _, _ ->
                    removeFriend(friendId) // Proceed to remove friend if confirmed
                }
                builder.setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()       // Dismiss the dialog on "No"
                }
                builder.show()
            }
            .addOnFailureListener { exception ->
                Log.e("FriendsProfileActivity", "Error fetching username: ", exception)
                Toast.makeText(this, "Error fetching friend info.", Toast.LENGTH_SHORT).show()
            }
    }

    // Removes the friend from the current user's friends list in Firestore
    private fun removeFriend(friendId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        val userRef = firestore.collection("users").document(currentUserId)

        userRef.update("friends", FieldValue.arrayRemove(friendId))
            .addOnSuccessListener {
                triggerRaindropEffect()

                Toast.makeText(this, "Friend removed successfully!", Toast.LENGTH_SHORT).show()

                // Update UI to reflect removal
                binding.addFriendButton.visibility = View.VISIBLE // Show add friend button
                binding.favoriteFriendBtn.visibility = View.GONE // Hide favorite button
                binding.removeFriendButton.visibility = View.GONE   // Hide unfriend button
            }
            .addOnFailureListener { exception ->
                Log.e("FriendsProfileActivity", "Error removing friend: ", exception)
                Toast.makeText(this, "Failed to remove friend.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun toggleFavoriteStatus(currentUserId: String, friendId: String) {
        val userRef = firestore.collection("users").document(currentUserId)

        userRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val favoriteFriendsList = document.get("favoriteFriends") as? List<String> ?: emptyList()

                // Check if the friendId is already in the favorites list
                if (favoriteFriendsList.contains(friendId)) {
                    // Friend is already a favorite, ask for confirmation to remove
                    showConfirmationDialog("Remove from Watchers List", "Are you sure you want to remove this friend from your Watchers List?") {
                        userRef.update("favoriteFriends", FieldValue.arrayRemove(friendId))
                            .addOnSuccessListener {

                                triggerBrokenHeartDropEffect()

                                Toast.makeText(this, "Removed from Watchers List", Toast.LENGTH_SHORT).show()
                                binding.favoriteFriendBtn.setImageResource(R.drawable.favorite) // Change to outline icon
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to remove from Watchers List: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    // Friend is not a favorite, ask for confirmation to add
                    showConfirmationDialog("Add to Watchers List", "Are you sure you want to add this friend to your Watchers List?") {
                        userRef.update("favoriteFriends", FieldValue.arrayUnion(friendId))
                            .addOnSuccessListener {
                                Toast.makeText(this, "Added to your Watchers List", Toast.LENGTH_SHORT).show()

                                triggerHeartDropEffect()

                                achievementManager.checkAndGrantCommunityBuilderBadge(userId)

                                binding.favoriteFriendBtn.setImageResource(R.drawable.favorite_filled) // Change to filled icon
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to add to favorites: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            } else {
                Log.e("FriendsProfileActivity", "User document does not exist")
                Toast.makeText(this, "User document not found.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Log.e("FriendsProfileActivity", "Error fetching user document: ", exception)
            Toast.makeText(this, "Error fetching user data.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showConfirmationDialog(title: String, message: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Yes") { dialog, _ ->
                onConfirm.invoke() // Call the onConfirm function
                dialog.dismiss() // Dismiss the dialog
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss() // Just dismiss the dialog
            }
            .show()
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

    private fun updateBadgesList(badges: List<String>) {
        // Filter the list of all badges based on fetched user badges
        val unlockedBadges = allBadges.filter { badge -> badges.contains(badge.id) }
        Log.d("ProfileFragment", "Unlocked badges: $unlockedBadges") // Log for debugging

        // Initialize or update the adapter
        if (!::badgesAdapter.isInitialized) {
            badgesAdapter = BadgesAdapter(unlockedBadges) { badge ->
                // Handle badge click
            }
            binding.badgesRecyclerView.adapter = badgesAdapter
            Log.d("ProfileFragment", "BadgesAdapter initialized with ${unlockedBadges.size} badges.")
        } else {
            badgesAdapter.updateBadges(unlockedBadges)
        }
    }

    // Initiates the friend addition process by calling the confirmation dialog
    private fun addFriend(friendId: String) {
        confirmAddFriend(friendId) // Show confirmation before proceeding
    }

    // Displays a confirmation dialog for adding a friend with the friend's username
    private fun confirmAddFriend(friendId: String) {
        // Fetch friend's username
        firestore.collection("users").document(friendId).get()
            .addOnSuccessListener { document ->
                val friendUsername = document.getString("username") ?: "this user"  // Use "this user" if username is missing

                // Show the confirmation dialog with the friend's username
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Add Friend")
                builder.setMessage("Are you sure you want to add $friendUsername?")
                builder.setPositiveButton("Yes") { _, _ ->
                    performAddFriend(friendId) // Proceed to add friend if confirmed
                }
                builder.setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()        // Dismiss the dialog on "No"
                }
                builder.show()
            }
            .addOnFailureListener { exception ->
                Log.e("FriendsProfileActivity", "Error fetching username: ", exception)
                Toast.makeText(this, "Error fetching friend info.", Toast.LENGTH_SHORT).show()
            }
    }

    // Adds the friend to the current user's friends list in Firestore
    private fun performAddFriend(friendId: String) {
        val currentUserId = auth.currentUser?.uid ?: return

        // Reference to the friend’s document in Firestore
        val friendRef = firestore.collection("users").document(friendId)

        friendRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val pendingRequests = document.get("pendingRequests") as? List<String> ?: emptyList()

                if (pendingRequests.contains(currentUserId)) {
                    Toast.makeText(this, "Friend request already sent.", Toast.LENGTH_SHORT).show()
                } else {
                    // Update the friend's document to add the current user's ID to pendingRequests
                    val friendUpdates = hashMapOf<String, Any>("pendingRequests" to FieldValue.arrayUnion(currentUserId))

                    friendRef.set(friendUpdates, SetOptions.merge())
                        .addOnSuccessListener {
                            Toast.makeText(this, "Friend request sent successfully!", Toast.LENGTH_SHORT).show()
                            binding.addFriendButton.visibility = View.GONE
                        }
                        .addOnFailureListener { exception ->
                            Log.e("FriendsProfileActivity", "Error sending friend request: ", exception)
                            Toast.makeText(this, "Failed to send friend request.", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                Log.e("FriendsProfileActivity", "Friend document does not exist")
                Toast.makeText(this, "User not found.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Log.e("FriendsProfileActivity", "Error fetching friend document: ", exception)
            Toast.makeText(this, "Error fetching user data.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun fetchFriendsInCommon() {
        val currentUserId = auth.currentUser?.uid ?: return

        // Get the friends list of the current user
        firestore.collection("users").document(currentUserId).get()
            .addOnSuccessListener { currentUserDocument ->
                if (currentUserDocument.exists()) {
                    val currentUserFriends = currentUserDocument.get("friends") as? List<String> ?: emptyList()

                    // Now get the friends list of the friend whose profile is being viewed
                    firestore.collection("users").document(userId).get()
                        .addOnSuccessListener { friendDocument ->
                            if (friendDocument.exists()) {
                                val friendFriends = friendDocument.get("friends") as? List<String> ?: emptyList()

                                // Find common friends
                                val commonFriendsIds = currentUserFriends.intersect(friendFriends).toList()

                                // Fetch the details of common friends
                                loadCommonFriendsData(commonFriendsIds)
                            }
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FriendsProfileActivity", "Error fetching current user friends: ", exception)
            }
    }

    private fun loadCommonFriendsData(commonFriendsIds: List<String>) {
        val commonFriends = mutableListOf<Friends>()
        val tasks = mutableListOf<Task<DocumentSnapshot>>()

        for (friendId in commonFriendsIds) {
            tasks.add(firestore.collection("users").document(friendId).get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val friend = Friends(
                        userId = friendId,
                        username = document.getString("username") ?: "Unknown",
                        profileImageUrl = document.getString("profileImageUrl"),
                        isPrivateAccount = document.getBoolean("isPrivateAccount") ?: false,
                        watcherVisible = document.getBoolean("watcherVisible") ?: false
                    )
                    commonFriends.add(friend)
                }
            })
        }

        // Wait until all tasks are completed
        Tasks.whenAllSuccess<DocumentSnapshot>(tasks).addOnSuccessListener {
            if (commonFriends.isNotEmpty()) {
                // Update the adapter with common friends data
                friendsInCommonAdapter.updateUserList(commonFriends)
                binding.friendsInCommonRecyclerView.visibility = View.VISIBLE // Show the RecyclerView
            } else {
                binding.friendsInCommonRecyclerView.visibility = View.GONE // Hide if no common friends
            }
        }.addOnFailureListener { exception ->
            Log.e("FriendsProfileActivity", "Error fetching common friends: ", exception)
        }
    }

    // Fetches users current location from Firestore
    private fun fetchCurrentUserLocation() {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val isShareLocationVisible = document.getBoolean("shareLocationVisible") ?: true

                    if (isShareLocationVisible) {
                        val city = document.getString("city") ?: "City not found"
                        val state = document.getString("state") ?: "State not found"
                        val zip = document.getString("zip") ?: "Zip not found"
                        val location = "$city, $state $zip"

                        showMessage("Location", location)
                    } else {
                        showMessage("Location", "Private")
                    }
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
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle(title)
        dialogBuilder.setMessage(message)
        dialogBuilder.setPositiveButton("OK", null)
        dialogBuilder.show()
    }

    private fun loadFavoriteParks() {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val favoriteParksList = document.get("favoriteParks") as? List<String> ?: emptyList()

                    // Assuming you have a function to fetch park details based on park IDs
                    fetchParksDetails(favoriteParksList)
                } else {
                    Log.e("FriendsProfileActivity", "Friend document does not exist")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FriendsProfileActivity", "Error fetching friend's favorite parks: ", exception)
            }
    }
    private fun fetchParksDetails(parkCodes: List<String>) {
        val tasks = parkCodes.map { parkCode ->
            RetrofitInstance.api.getParkDetails(parkCode)
        }

        // Track the number of responses
        var completedRequests = 0

        tasks.forEach { call ->
            call.enqueue(object : Callback<NPSResponse> {
                override fun onResponse(call: Call<NPSResponse>, response: Response<NPSResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val park = response.body()?.data?.firstOrNull()
                        park?.let {
                            favoriteParks.add(it) // Add the park to the list
                        }
                    }
                    completedRequests++
                    // Check if all requests are completed
                    if (completedRequests == parkCodes.size) {
                        updateParksRecyclerView(favoriteParks)
                    }
                }

                override fun onFailure(call: Call<NPSResponse>, t: Throwable) {
                    Log.e("FavoritesFragment", "Error fetching park details: ${t.message}")
                    completedRequests++
                    // Check if all requests are completed, even on failure
                    if (completedRequests == parkCodes.size) {
                        updateParksRecyclerView(favoriteParks)
                    }
                }
            })
        }
    }

    private fun updateParksRecyclerView(parks: List<Park>) {
        favoritesAdapter.updateData(parks) // Update the adapter with the fetched parks
    }

    private fun fetchLeaderboard() {
        firestore.collection("users").get()
            .addOnSuccessListener { querySnapshot ->
                val leaderboardEntries = mutableListOf<LeaderboardEntry>()

                for (document in querySnapshot.documents) {
                    val userId = document.id
                    val username = document.getString("username") ?: "Unknown"
                    val badges = document.get("badges") as? List<String> ?: emptyList()
                    val badgeCount = badges.size

                    // Get the profile image URL
                    val profileImageUrl = document.getString("profileImageUrl") // Assuming this field exists

                    // Add to leaderboard entries
                    leaderboardEntries.add(LeaderboardEntry(userId, username, badgeCount, profileImageUrl))
                }

                // Sort the leaderboard entries by badge count in descending order
                leaderboardEntries.sortByDescending { it.badgeCount }

                // Update the RecyclerView with the sorted leaderboard
                updateLeaderboardRecyclerView(leaderboardEntries)
            }
            .addOnFailureListener { e ->
                Log.e("ProfileFragment", "Error fetching leaderboard: ", e)
            }
    }

    private fun updateLeaderboardRecyclerView(entries: List<LeaderboardEntry>) {
        val leaderboardAdapter = LeaderboardAdapter(entries)
        binding.leaderRecyclerView.adapter = leaderboardAdapter
    }

    // Fetches the friend's photos based on their userId
    private fun fetchFriendPhotos(friendUserId: String) {
        firestore.collection("users").document(friendUserId)
            .collection("photos")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val newPhotoUrls = mutableListOf<String>()
                for (document in querySnapshot.documents) {
                    val url = document.getString("url")
                    if (url != null) {
                        newPhotoUrls.add(url)
                    }
                }
                photosAdapter.updatePhotos(newPhotoUrls)
            }
            .addOnFailureListener { e ->
                Log.e("FriendsProfileActivity", "Error fetching photos", e)
            }
    }

    private fun fetchTimeRecordsForFriend(friendId: String) {
        firestore.collection("users").document(friendId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val timeRecordsData = document.get("timeRecords") as? List<Map<String, Any>>
                    val timeRecords = timeRecordsData?.map { record ->
                        val parkName = record["parkName"] as? String ?: return@map null
                        val elapsedTime = record["elapsedTime"] as? String ?: return@map null
                        val parkCode = record["parkCode"] as? String ?: return@map null
                        val imageUrl = record["imageUrl"] as? String // This can be null
                        val timestamp = record["timestamp"] as? Long ?: return@map null // Assuming timestamp is a Long
                        val date = record["date"] as? String ?: return@map null // Assuming date is a String

                        // Create a TimeRecord object
                        TimeRecord(parkName, elapsedTime, imageUrl, parkCode, timestamp, date)
                    }?.filterNotNull() ?: emptyList() // Filter out any null items

                    // Update the adapter with fetched data using updateData method
                    timeRecordAdapter.updateData(timeRecords)
                }
            }
            .addOnFailureListener { e ->
                Log.e("FriendsProfileActivity", "Error fetching time records: ${e.message}")
            }
    }

    private fun triggerRaindropEffect() {
        val handler = Handler(Looper.getMainLooper())

        for (i in 0 until 20) {  // Number of raindrops
            val delay = (0 .. 1000).random().toLong()   // Random delay between 0 and 1 second

            handler.postDelayed({
                val raindrop = ImageView(this)
                raindrop.setImageResource(R.drawable.raindrop2)

                // Set position and animation
                val params = RelativeLayout.LayoutParams(30, 1500)
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP)

                params.leftMargin = (0..binding.root.width).random()

                raindrop.layoutParams = params
                binding.root.addView(raindrop)

                // Start animation
                val animation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.raindrop_fall).apply{
                    duration = (1000..2000).random().toLong()
                }
                raindrop.startAnimation(animation)

                // Remove view after animation
                animation.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
                    override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                        binding.root.removeView(raindrop)
                    }

                    override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
                    override fun onAnimationStart(animation: android.view.animation.Animation?) {}
                })
            }, delay)
        }
    }

    private fun triggerHeartDropEffect() {
        val handler = Handler(Looper.getMainLooper())

        for (i in 0 until 20) {  // Number of raindrops
            val delay = (0 .. 1000).random().toLong()   // Random delay between 0 and 1 second

            handler.postDelayed({
                val raindrop = ImageView(this)
                raindrop.setImageResource(R.drawable.heart)

                // Set position and animation
                val params = RelativeLayout.LayoutParams(90, 1500)
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP)

                params.leftMargin = (0..binding.root.width).random()

                raindrop.layoutParams = params
                binding.root.addView(raindrop)

                // Start animation
                val animation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.raindrop_fall).apply{
                    duration = (1000..2000).random().toLong()
                }
                raindrop.startAnimation(animation)

                // Remove view after animation
                animation.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
                    override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                        binding.root.removeView(raindrop)
                    }

                    override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
                    override fun onAnimationStart(animation: android.view.animation.Animation?) {}
                })
            }, delay)
        }
    }

    private fun triggerBrokenHeartDropEffect() {
        val handler = Handler(Looper.getMainLooper())

        for (i in 0 until 20) {  // Number of raindrops
            val delay = (0 .. 1000).random().toLong()   // Random delay between 0 and 1 second

            handler.postDelayed({
                val raindrop = ImageView(this)
                raindrop.setImageResource(R.drawable.broken_heart)

                // Set position and animation
                val params = RelativeLayout.LayoutParams(90, 1500)
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP)

                params.leftMargin = (0..binding.root.width).random()

                raindrop.layoutParams = params
                binding.root.addView(raindrop)

                // Start animation
                val animation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.raindrop_fall).apply{
                    duration = (1000..2000).random().toLong()
                }
                raindrop.startAnimation(animation)

                // Remove view after animation
                animation.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
                    override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                        binding.root.removeView(raindrop)
                    }

                    override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
                    override fun onAnimationStart(animation: android.view.animation.Animation?) {}
                })
            }, delay)
        }
    }
}