package com.byteforce.trailblaze.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byteforce.trailblaze.R
import com.byteforce.trailblaze.databinding.FragmentProfileBinding
import com.byteforce.trailblaze.firestore.UserRepository
import com.byteforce.trailblaze.ui.achievements.AchievementManager
import com.byteforce.trailblaze.ui.achievements.BadgesAdapter
import com.byteforce.trailblaze.ui.achievements.Badge
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.byteforce.trailblaze.firestore.ImageLoader
import com.byteforce.trailblaze.firestore.UserManager
import com.byteforce.trailblaze.nps.NPSResponse
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.byteforce.trailblaze.nps.RetrofitInstance
import com.byteforce.trailblaze.ui.Map.LocationItem
import com.byteforce.trailblaze.ui.MenuActivity
import com.byteforce.trailblaze.ui.parks.ParkDetailActivity
import com.google.firebase.appcheck.internal.util.Logger.TAG
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import com.byteforce.trailblaze.watcherFeature.WatcherMemberList
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest

interface PhotoDeletionListener {
    fun onPhotoDeleted(deletedPhotoUrl: String)
}

    class ProfileFragment : Fragment(), PhotoDeletionListener {

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

        private lateinit var favoritesRecyclerView: RecyclerView
        private lateinit var favoritesAdapter: FavoritesAdapter

        private val PICK_IMAGE_REQUEST = 1
        private var selectedImageUri: Uri? = null

        private val photoUrls = mutableListOf<String>()
        private lateinit var photosAdapter: PhotosAdapter

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


            // Initialize RecyclerView
            favoritesRecyclerView = binding.favoriteParksRecyclerView
            favoritesRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            setupFavoritesRecyclerView()
            loadFavoriteParks()
            fetchLeaderboard()


            // Initialize the icons
            binding.iconLocation.setOnClickListener {
                fetchCurrentUserLocation()
            }

            // Watcher Member Profile Listener
            binding.watcherMember.setOnClickListener {
                val intent = Intent(context, WatcherMemberList::class.java)
                startActivity(intent)
            }


            // Message Icon Listener
            binding.messageIcon.setOnClickListener {
                val intent = Intent(context, InboxActivity::class.java)
                startActivity(intent)

            }

            binding.iconDifficulty.setOnClickListener {
                fetchCurrentUserDifficulty()
            }

            binding.uploadPhotoButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, PICK_IMAGE_REQUEST)
            }

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
            binding.menuButton.setOnClickListener {
                val intent = Intent(activity, MenuActivity::class.java)
                startActivity(intent)
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

            achievementManager = AchievementManager(requireContext())
            badgesList = binding.badgesRecyclerView
            badgesList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        }

        private fun fetchUserFriends() {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                firestore.collection("users").document(userId).get()
                    .addOnSuccessListener { document ->

                        val friendsList = document.get("friends") as? List<String> ?: emptyList()
                        updateFriendsCount(friendsList.size)

                        if (document.exists()) {
                            val friendsIds = document.get("friends") as? List<String> ?: emptyList()
                            loadFriendsData(friendsIds)
                            achievementManager.checkAndGrantSocialButterflyBadge(userId)

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
                        profileImageUrl = document.getString("profileImageUrl"),
                        isPrivateAccount = document.getBoolean("isPrivateAccount") ?: false,
                        watcherVisible = document.getBoolean("watcherVisible") ?: false
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
                            // Retrieve the badges as a List<String>
                            val badges = document.get("badges") as? List<String> ?: emptyList()
                            Log.d("ProfileFragment", "Fetched badges: $badges") // Log for debugging
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
                        val city = document.getString("city") ?: "City not found"
                        val state = document.getString("state") ?: "State not found"
                        val zip = document.getString("zip") ?: "Zip not found"

                        // Combine city, state, and zip into a single string
                        val location = "$city, $state $zip"
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

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
                selectedImageUri = data.data  // Assign the URI to the property
                uploadImageToFirebase() // Call the upload function
            }
        }

        private fun uploadImageToFirebase() {
            val storageRef = FirebaseStorage.getInstance().reference
            val photoRef = storageRef.child("profilePhotos/${UUID.randomUUID()}.jpg")

            selectedImageUri?.let { uri ->
                photoRef.putFile(uri).addOnSuccessListener {
                    photoRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        savePhotoToFirestore(downloadUri.toString())  // Save the URL to Firestore
                    }
                }.addOnFailureListener {
                    // Handle any errors here
                }
            }
        }

        private fun savePhotoToFirestore(photoUrl: String) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                val firestore = FirebaseFirestore.getInstance()

                val photoData = hashMapOf(
                    "url" to photoUrl,
                    "timestamp" to FieldValue.serverTimestamp(),
                )

            firestore.collection("users")
                .document(userId)
                .collection("photos")
                .add(photoData)
                .addOnSuccessListener {
                    Log.d("ProfileFragment", "Photo successfully added to Firestore!")

                    achievementManager.checkAndGrantPhotographerBadge(userId)

                    achievementManager.checkAndGrantLeaderboardBadge()


                    fetchPhotos() // Call fetchPhotos after successfully saving
                }
                .addOnFailureListener { e ->
                    Log.w("ProfileFragment", "Error adding photo to Firestore", e)
                }
        }
    }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            val recyclerView: RecyclerView = view.findViewById(R.id.photosRecyclerView)
            photosAdapter = PhotosAdapter(photoUrls, isOwnProfile = true)
            recyclerView.adapter = photosAdapter

            fetchPhotos()
        }

        private fun fetchPhotos() {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            val firestore = FirebaseFirestore.getInstance()

            userId?.let {
                firestore.collection("users").document(it)
                    .collection("photos")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        val newPhotoUrls = mutableListOf<String>() // Create a new list to hold the URLs
                        for (document in querySnapshot.documents) {
                            val url = document.getString("url")
                            if (url != null) {
                                newPhotoUrls.add(url)
                            }
                        }

                        // Update the adapter with the new photo URLs
                        photosAdapter.updatePhotos(newPhotoUrls)

                        // Update the photo count dynamically
                        updatePhotosCount(newPhotoUrls.size)
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error fetching photos", e)
                    }
            }
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

        override fun onPhotoDeleted(photoUrl: String) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                // Find the specific document to delete
                firestore.collection("users").document(userId)
                    .collection("photos")
                    .whereEqualTo("url", photoUrl)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        if (querySnapshot.isEmpty) {
                            Log.d("ProfileFragment", "No photo found with the given URL.")
                            return@addOnSuccessListener
                        }
                        // Iterate through matching documents and delete them
                        for (document in querySnapshot.documents) {
                            document.reference.delete()
                                .addOnSuccessListener {
                                    Log.d("ProfileFragment", "Photo document deleted successfully: ${document.id}.")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Firestore", "Error deleting photo document: ", e)
                                }
                        }
                        fetchPhotos() // Refresh photos after deletion
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error finding photo document: ", e)
                    }
            }
        }

        private fun updateFriendsCount(count: Int) {
            val friendsTitle = view?.findViewById<TextView>(R.id.yourFriendsTitle) ?: return
            friendsTitle.text = getString(R.string.friends, count)
        }

        //Function to set up Favorites RecyclerView for both parks and places
        private fun setupFavoritesRecyclerView() {
            favoritesAdapter = FavoritesAdapter(emptyList()) { item ->
                when (item) {
                    is LocationItem.ParkItem -> {
                        val intent = Intent(context, ParkDetailActivity::class.java)
                        intent.putExtra("PARK_CODE", item.park.parkCode)
                        startActivity(intent)
                    }
                    is LocationItem.PlaceItem -> {
                        val intent = Intent(context, ParkDetailActivity::class.java)
                        intent.putExtra("PLACE_ID", item.place.id)
                        startActivity(intent)
                    }
                }
            }
            favoritesRecyclerView.adapter = favoritesAdapter
        }

        //fucntion to load both parks and or places
        private fun loadFavoriteParks() {
            val userId = auth.currentUser?.uid ?: return

            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->

                    val favoriteTrails = document.get("favoriteParks") as? List<String> ?: emptyList()
                    updateFavoriteTrailsCount(favoriteTrails.size)

                    if (document != null && document.exists()) {
                        val favoriteParksList = document.get("favoriteParks") as? List<String> ?: emptyList()
                        val locationItems = mutableListOf<LocationItem>()

                        // Separate park codes and place IDs
                        val parkCodes = favoriteParksList.filter { it.length < 10 }
                        val placeIds = favoriteParksList.filter { it.length >= 10 }

                        var totalLoaded = 0
                        val totalItems = parkCodes.size + placeIds.size

                        // Fetch parks from NPS API
                        parkCodes.forEach { parkCode ->
                            RetrofitInstance.api.getParkDetails(parkCode).enqueue(object : Callback<NPSResponse> {
                                override fun onResponse(call: Call<NPSResponse>, response: Response<NPSResponse>) {
                                    response.body()?.data?.firstOrNull()?.let { park ->
                                        locationItems.add(LocationItem.ParkItem(park))
                                    }
                                    totalLoaded++
                                    checkAndUpdateAdapter(totalLoaded, totalItems, locationItems)
                                }

                                override fun onFailure(call: Call<NPSResponse>, t: Throwable) {
                                    totalLoaded++
                                    checkAndUpdateAdapter(totalLoaded, totalItems, locationItems)
                                }
                            })
                        }

                        // Fetch places from Google Places API
                        val placeFields = listOf(
                            Place.Field.ID,
                            Place.Field.DISPLAY_NAME,
                            Place.Field.PHOTO_METADATAS,
                            Place.Field.LOCATION
                        )

                        placeIds.forEach { placeId ->
                            val request = FetchPlaceRequest.builder(placeId, placeFields).build()
                            context?.let { ctx ->
                                Places.createClient(ctx).fetchPlace(request)
                                    .addOnSuccessListener { response ->
                                        locationItems.add(LocationItem.PlaceItem(response.place))
                                        totalLoaded++
                                        checkAndUpdateAdapter(totalLoaded, totalItems, locationItems)
                                    }
                                    .addOnFailureListener {
                                        totalLoaded++
                                        checkAndUpdateAdapter(totalLoaded, totalItems, locationItems)
                                    }
                            }
                        }
                    }
                }
        }

        private fun updatePhotosCount(count: Int) {
            val photosTitle = view?.findViewById<TextView>(R.id.userPhotosTextView) ?: return
            photosTitle.text = getString(R.string.userPhotos, count)
        }

        private fun updateFavoriteTrailsCount(count: Int) {
            val favoriteTrailsTitle = view?.findViewById<TextView>(R.id.favoriteTrailsHeader)
            favoriteTrailsTitle?.text = getString(R.string.userFavoriteTrails, count)
        }

        private fun checkAndUpdateAdapter(loaded: Int, total: Int, items: List<LocationItem>) {
            if (loaded == total) {
                favoritesAdapter.updateData(items)
            }
        }

        override fun onResume() {
            super.onResume()
            fetchPhotos()
        }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }
    }