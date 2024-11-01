package com.example.trailblaze.ui.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

    private lateinit var photosAdapter: PhotosAdapter
    private val photoUrls = mutableListOf<String>()

    private lateinit var favoritesRecyclerView: RecyclerView
    private lateinit var favoritesAdapter: ParksAdapter
    private var favoriteParks: MutableList<Park> = mutableListOf()




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
        friendsInCommonAdapter = FriendAdapter(friendsInCommonList) { friend ->
            // Handle friend click here if needed
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

        // Initialize RecyclerView and Adapter
        val recyclerView = findViewById<RecyclerView>(R.id.photosRecyclerView)
        photosAdapter = PhotosAdapter(photoUrls)
        recyclerView.adapter = photosAdapter
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Initialize the "Add" button
        addFriendButton = binding.addFriendButton

        addFriendButton.setOnClickListener {
            addFriend(userId) // Call the function to add friend
        }

        favoritesRecyclerView = binding.favoriteTrailsSection
        favoritesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        favoritesAdapter = ParksAdapter(emptyList()) { park ->
            val intent = Intent(this, ParkDetailActivity::class.java).apply {
                putExtra("PARK_CODE", park.parkCode)
            }
            startActivity(intent)
        }
        favoritesRecyclerView.adapter = favoritesAdapter
    }

    private fun loadFriendProfile() {
        val currentUserId = auth.currentUser?.uid ?: return // Get the current user's ID

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val username = document.getString("username")
                    val imageUrl = document.getString("profileImageUrl")
                    val favoritePark = document.get("favoriteParks")

                    binding.username.text = username
                    ImageLoader.loadProfilePicture(this, binding.profilePicture, imageUrl)

                    // Retrieve visibility settings
                    val isLeaderboardVisible = document.getBoolean("leaderboardVisible") ?: true
                    val isPhotosVisible = document.getBoolean("photosVisible") ?: true
                    val isFavoriteTrailsVisible = document.getBoolean("favoriteTrailsVisible") ?: true
                    val isWatcherVisible = document.getBoolean("watcherVisible") ?: true

                    // Set visibility based on the privacy settings
                    binding.leaderboardSection.visibility = if (isLeaderboardVisible) View.VISIBLE else View.GONE
                    binding.leaderboardHeader.visibility = if (isLeaderboardVisible) View.VISIBLE else View.GONE

                    binding.photosRecyclerView.visibility = if (isPhotosVisible) View.VISIBLE else View.GONE
                    binding.photosHeader.visibility = if (isPhotosVisible) View.VISIBLE else View.GONE

                    binding.favoriteTrailsSection.visibility = if (isFavoriteTrailsVisible) View.VISIBLE else View.GONE
                    binding.favoriteTrailsHeader.visibility = if (isFavoriteTrailsVisible) View.VISIBLE else View.GONE

                    binding.watcherMember.visibility = if (isWatcherVisible) View.VISIBLE else View.GONE

                    // Check if the current user is friends with this friend
                    checkFriendshipStatus(currentUserId, userId)

                    // Fetch and display badges
                    val badges = document.get("badges") as? List<String> ?: emptyList()
                    updateBadgesList(badges)

                    // Fetch and display the friend's photos if the photos section is visible
                    if (isPhotosVisible) {
                        fetchFriendPhotos(userId)
                    }
                } else {
                    Log.e("FriendsProfileActivity", "Friend document does not exist")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FriendsProfileActivity", "Error fetching friend profile: ", exception)
            }
    }


    private fun checkFriendshipStatus(currentUserId: String, friendId: String) {
        firestore.collection("users").document(currentUserId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val friendsList = document.get("friends") as? List<String> ?: emptyList()
                    val favoriteFriendsList = document.get("favoriteFriends") as? List<String> ?: emptyList()

                    // Check if the friendId exists in the friends list
                    if (friendsList.contains(friendId)) {
                        // They are friends, show favorite button and hide add friend button
                        binding.favoriteFriendBtn.visibility = View.VISIBLE // Show favorite icon
                        binding.addFriendButton.visibility = View.GONE // Hide add friend button
                    } else {
                        // They are not friends, show add friend button and hide favorite button
                        binding.favoriteFriendBtn.visibility = View.GONE // Hide favorite icon
                        binding.addFriendButton.visibility = View.VISIBLE // Show add friend button
                    }

                    // Check if the friend is already in the favorites list
                    if (favoriteFriendsList.contains(friendId)) {
                        Toast.makeText(this, "Friend added to favorites successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Friend already a favorite friend.", Toast.LENGTH_SHORT).show()
                    }

                    // Set up click listener for the favorite button
                    binding.favoriteFriendBtn.setOnClickListener {
                        if (favoriteFriendsList.contains(friendId)) {
                            Toast.makeText(this, "This friend is already in your favorites.", Toast.LENGTH_SHORT).show()
                        } else {
                            addFavoriteFriend(currentUserId, friendId)
                        }
                    }

                }
            }
            .addOnFailureListener { exception ->
                Log.e("FriendsProfileActivity", "Error fetching user friends: ", exception)
            }
    }

    private fun addFavoriteFriend(currentUserId: String, friendId: String) {
        val userRef = firestore.collection("users").document(currentUserId)

        userRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                // Get the favorite friends list from the document
                val favoriteFriendsList = document.get("favoriteFriends") as? List<String> ?: emptyList()

                // Check if the friendId is already in the favorites list
                if (favoriteFriendsList.contains(friendId)) {
                    Toast.makeText(this, "This friend is already in your favorites.", Toast.LENGTH_SHORT).show()
                } else {
                    // Create a HashMap to represent the user's document
                    val userUpdates = HashMap<String, Any>()
                    userUpdates["favoriteFriends"] = FieldValue.arrayUnion(friendId) // Add friendId to the array

                    // Update the current user's document in Firestore
                    userRef.set(userUpdates, SetOptions.merge())
                        .addOnSuccessListener {
                            Toast.makeText(this, "Friend added to favorites successfully!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { exception ->
                            Log.e("FriendsProfileActivity", "Error adding friend to favorites: ", exception)
                            Toast.makeText(this, "Failed to add friend to favorites.", Toast.LENGTH_SHORT).show()
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

    private fun addFriend(friendId: String) {
        val currentUserId = auth.currentUser?.uid // Get the current user's ID

        if (currentUserId != null) {
            // Reference to the current user's document
            val userRef = firestore.collection("users").document(currentUserId)

            // Fetch the current user's document to check their friends list
            userRef.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Get the friends list from the document
                        val friendsList = document.get("friends") as? List<String> ?: emptyList()

                        // Check if the friendId is already in the friends list
                        if (friendsList.contains(friendId)) {
                            Toast.makeText(this, "This user is already your friend.", Toast.LENGTH_SHORT).show()
                        } else {
                            // Create a HashMap to represent the user's document
                            val userUpdates = HashMap<String, Any>()
                            userUpdates["friends"] = FieldValue.arrayUnion(friendId)

                            // Update the current user's document in Firestore
                            userRef.set(userUpdates, SetOptions.merge())
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Friend added successfully!", Toast.LENGTH_SHORT).show()

                                    achievementManager.checkAndGrantSocialButterflyBadge(currentUserId)
                                }
                                .addOnFailureListener { exception ->
                                    Log.e("FriendsProfileActivity", "Error adding friend: ", exception)
                                    Toast.makeText(this, "Failed to add friend.", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        Log.e("FriendsProfileActivity", "User document does not exist")
                        Toast.makeText(this, "User document not found.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("FriendsProfileActivity", "Error fetching user document: ", exception)
                    Toast.makeText(this, "Error fetching user data.", Toast.LENGTH_SHORT).show()
                }
        } else {
            Log.e("FriendsProfileActivity", "Current user ID is null")
            Toast.makeText(this, "You are not logged in.", Toast.LENGTH_SHORT).show()
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
                        profileImageUrl = document.getString("profileImageUrl")
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
}

