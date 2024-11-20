package com.example.trailblaze.ui.parks

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.BuildConfig
import com.example.trailblaze.R
import com.example.trailblaze.nps.NPSResponse
import com.example.trailblaze.nps.Park
import com.example.trailblaze.nps.RetrofitInstance
import com.example.trailblaze.ui.achievements.AchievementManager
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.libraries.places.api.Places
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

// Activity where the user attempts a trail
class AttemptTrailActivity : AppCompatActivity() {

    private lateinit var parkCode: String
    private lateinit var parkName: String // Store the park name
    private var placeId: String? = null
    private lateinit var locationName: String
    private lateinit var parkImageUrl: String
    private lateinit var parkNameTextView: TextView
    private lateinit var soloJourneyCheckBox: CheckBox
    private lateinit var partyInfoLayout: LinearLayout
    private lateinit var startTrailButton: Button
    private lateinit var firestore: FirebaseFirestore
    private lateinit var achievementManager: AchievementManager

    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private var userList: MutableList<NonTrailBlazeUser> = mutableListOf()
    private lateinit var activities: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attempt_trail)
        supportActionBar?.hide()
        // Initialize Places API right at the start
        Places.initialize(applicationContext, BuildConfig.PLACES_API_KEY)
        // Initialize core components
        firestore = FirebaseFirestore.getInstance()
        achievementManager = AchievementManager(this)
        parkNameTextView = findViewById(R.id.parkNameTextView)

        // Back button setup
        findViewById<ImageButton>(R.id.chevron_left).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Get data from intent
        parkCode = intent.getStringExtra("PARK_CODE") ?: ""
        placeId = intent.getStringExtra("PLACE_ID")
        activities = intent.getStringArrayExtra("PARK_ACTIVITIES") ?: arrayOf()

        // Set location name based on data source
        locationName = when {
            !placeId.isNullOrEmpty() -> intent.getStringExtra("PLACE_NAME") ?: "Unknown Place"
            parkCode.isNotEmpty() -> {
                fetchParkDetails(parkCode)
                intent.getStringExtra("PARK_NAME") ?: "Unknown Park"
            }
            else -> "Unknown Location"
        }
        parkNameTextView.text = locationName

        // Initialize UI components
        initializeUIComponents()

        // Set up RecyclerView and party members
        setupRecyclerView()
        addCurrentUserToParty()
        updatePartyMemberCount()
    }

    private fun initializeUIComponents() {
        soloJourneyCheckBox = findViewById(R.id.solo_journey_checkbox)
        partyInfoLayout = findViewById(R.id.party_info_layout)
        startTrailButton = findViewById(R.id.start_trail_button)

        soloJourneyCheckBox.setOnCheckedChangeListener { _, isChecked ->
            partyInfoLayout.visibility = if (isChecked) View.GONE else View.VISIBLE
        }

        setupButtons()
    }

    private fun setupButtons() {
        startTrailButton.setOnClickListener {
            savePartyMembersToFirestore()
            val partyMembers = userList.map { it.name }
            val intent = Intent(this, TimerActivity::class.java).apply {
                // Send Park data if available
                putExtra("PARK_CODE", parkCode)
                putExtra("PARK_ACTIVITIES", activities)

                // Send Place data if available
                putExtra("PLACE_ID", placeId)
                putExtra("PLACE_NAME", locationName)

                putStringArrayListExtra("PARTY_MEMBERS", ArrayList(partyMembers))
            }
            startActivity(intent)
        }

        findViewById<Button>(R.id.add_non_TrailBlaze_user).setOnClickListener {
            showAddUserDialog()
        }

        findViewById<Button>(R.id.add_trailMates_user).setOnClickListener {
            showAddFriendsDialog()
        }
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.partyMemberRecyclerView)
        userAdapter = UserAdapter(this, userList)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = userAdapter
    }

    private fun showAddFriendsDialog() {
        // Inflate the dialog layout
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_friends, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)

        val dialog = dialogBuilder.create()

        // Initialize RecyclerView
        val friendsRecyclerView: RecyclerView = dialogView.findViewById(R.id.friendsRecyclerView)
        val confirmButton: Button = dialogView.findViewById(R.id.confirm_selection_button)

        // Initialize an empty friends list
        val friendsList = mutableListOf<AddFriend>()

        // Initialize the adapter with the friends list
        val friendsAdapter = AddFriendsAdapter(friendsList) { friend ->
            // Handle checkbox change if necessary (optional)
        }

        // Set up the RecyclerView
        friendsRecyclerView.layoutManager = LinearLayoutManager(this)
        friendsRecyclerView.adapter = friendsAdapter

        // Fetch the user's friends
        fetchUserFriends { fetchedFriends ->
            friendsList.clear() // Clear the current list
            friendsList.addAll(fetchedFriends) // Add the fetched friends
            friendsAdapter.notifyDataSetChanged() // Notify the adapter to refresh the list
        }

        // Confirm button click listener
        confirmButton.setOnClickListener {
            // Loop through the friends list and add selected friends to the userList
            for (friend in friendsList) {
                if (friend.isSelected) {
                    userList.add(NonTrailBlazeUser(friend.username)) // Add selected friend's username
                }
            }
            userAdapter.notifyDataSetChanged() // Notify the user adapter to refresh the list
            updatePartyMemberCount() // Update the member count
            dialog.dismiss() // Close the dialog
        }

        dialog.show() // Show the dialog
    }


    private fun fetchUserFriends(onComplete: (List<AddFriend>) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val friendsIds = document.get("friends") as? List<String> ?: emptyList()
                        loadFriendsData(friendsIds, onComplete)
                    } else {
                        onComplete(emptyList()) // No friends found
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error fetching friends: ", e)
                    onComplete(emptyList()) // In case of an error, return empty list
                }
        } else {
            onComplete(emptyList()) // User not logged in
        }
    }

    private fun loadFriendsData(friendIds: List<String>, onComplete: (List<AddFriend>) -> Unit) {
        val tasks = mutableListOf<Task<DocumentSnapshot>>()
        val friendsList = mutableListOf<AddFriend>()

        for (friendId in friendIds) {
            tasks.add(firestore.collection("users").document(friendId).get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val username = document.getString("username") ?: "Unknown"
                    friendsList.add(AddFriend(username, friendId, false)) // Add friend to the list
                }
            })
        }

        // Wait until all friend data is fetched
        Tasks.whenAllSuccess<DocumentSnapshot>(tasks).addOnSuccessListener {
            onComplete(friendsList) // Return the populated friends list
        }
    }

    private fun addCurrentUserToParty() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid // Get the current user's ID

        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val username = document.getString("username") ?: "Unknown User" // Fetch the username
                        // Add the current user's username to the user list
                        userList.add(NonTrailBlazeUser(username))
                        userAdapter.notifyItemInserted(userList.size - 1) // Notify adapter of new item
                    } else {
                        // Handle the case where the document does not exist
                        userList.add(NonTrailBlazeUser("Username not found"))
                        userAdapter.notifyItemInserted(userList.size - 1)
                    }
                }
                .addOnFailureListener {
                    // Handle failure
                    userList.add(NonTrailBlazeUser("Error fetching username"))
                    userAdapter.notifyItemInserted(userList.size - 1)
                }
        } else {
            // Handle the case where userId is null (not logged in)
            userList.add(NonTrailBlazeUser("Not logged in"))
            userAdapter.notifyItemInserted(userList.size - 1)
        }
    }
    private fun showAddUserDialog() {
        // Inflate the custom layout for adding a user
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_user, null)

        // Create the dialog
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)

        val dialog = dialogBuilder.create()

        // Initialize the views in the dialog
        val editTextUserName: EditText = dialogView.findViewById(R.id.non_trailBlaze_user_name_input)
        val buttonSaveUser: Button = dialogView.findViewById(R.id.save_user_button)

        buttonSaveUser.setOnClickListener {
            val userName = editTextUserName.text.toString().trim()
            if (userName.isNotEmpty()) {
                // Add the new user to the list
                userList.add(NonTrailBlazeUser(userName))
                userAdapter.notifyItemInserted(userList.size - 1) // Notify adapter of new item

                // Update the party member count
                updatePartyMemberCount()

                editTextUserName.text.clear() // Clear the EditText for the next input
                dialog.dismiss() // Close the dialog
            } else {
                Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }
    private fun updatePartyMemberCount() {
        val partyMemberCountTextView: TextView = findViewById(R.id.party_member_count)
        partyMemberCountTextView.text = "(${userList.size})"  // Update the count in the format (N)
    }

    private fun fetchParkDetails(parkCode: String) {
        RetrofitInstance.api.getParkDetails(parkCode).enqueue(object : Callback<NPSResponse> {
            override fun onResponse(call: Call<NPSResponse>, response: Response<NPSResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { npsResponse ->
                        val park = npsResponse.data.firstOrNull()
                        park?.let {
                            populateParkDetails(it) // Pass the park object to populate the UI
                        } ?: run {
                            Toast.makeText(this@AttemptTrailActivity, "Park details not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<NPSResponse>, t: Throwable) {
                Toast.makeText(this@AttemptTrailActivity, "Error fetching park details: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun populateParkDetails(park: Park) {
        // Populate UI elements with park details
        parkNameTextView.text = park.fullName
        parkName = park.fullName // Store the park name
        parkImageUrl = park.images.firstOrNull()?.url ?: ""
    }

    private fun savePartyMembersToFirestore() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            // Reference to the user's document in Firestore
            val userDocRef = firestore.collection("users").document(userId)

            // Convert userList to a list of usernames
            val partyMembers = userList.map { it.name }

            // Update the partyMembers field, creating it if it doesn't exist
            userDocRef.update("partyMembers", partyMembers)
                .addOnSuccessListener {
                    Log.d("AttemptTrailActivity", "Party members saved successfully")
                    // Check and grant Team Player badge after saving party members
                    achievementManager.checkAndGrantTeamPlayerBadge(true) // Assume true for party hike
                    achievementManager.saveBadgeToUserProfile("teamplayer")
                }
                .addOnFailureListener { e ->
                    Log.e("AttemptTrailActivity", "Error saving party members: ${e.message}")
                }
        } else {
            Log.e("AttemptTrailActivity", "User not authenticated")
        }
    }
}