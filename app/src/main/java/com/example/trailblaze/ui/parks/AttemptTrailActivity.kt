package com.example.trailblaze.ui.parks

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.R
import com.example.trailblaze.nps.NPSResponse
import com.example.trailblaze.nps.Park
import com.example.trailblaze.nps.RetrofitInstance
import com.example.trailblaze.ui.achievements.AchievementManager
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import nl.dionsegijn.konfetti.KonfettiView
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AttemptTrailActivity : AppCompatActivity() {

    private lateinit var parkCode: String
    private lateinit var parkName: String // Store the park name
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attempt_trail)
        supportActionBar?.hide()

        firestore = FirebaseFirestore.getInstance()

        findViewById<ImageButton>(R.id.chevron_left).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Initialize AchievementManager
        achievementManager = AchievementManager(this)

        // Get the park code from the intent
        parkCode = intent.getStringExtra("PARK_CODE") ?: ""
        parkNameTextView = findViewById(R.id.parkNameTextView)
        fetchParkDetails(parkCode)

        Log.d("AttemptTrailActivity", "Received park code: $parkCode")

        soloJourneyCheckBox = findViewById(R.id.solo_journey_checkbox)
        partyInfoLayout = findViewById(R.id.party_info_layout)
        startTrailButton = findViewById(R.id.start_trail_button)

        soloJourneyCheckBox.setOnCheckedChangeListener { _, isChecked ->
            partyInfoLayout.visibility = if (isChecked) View.GONE else View.VISIBLE
        }

        startTrailButton.setOnClickListener {
            showTimerDialog()
            savePartyMembersToFirestore()
        }

        // Find the button and set the click listener
        val addNonTrailBlazeUserButton: Button = findViewById(R.id.add_non_TrailBlaze_user)
        addNonTrailBlazeUserButton.setOnClickListener {
            showAddUserDialog()
        }

        val addTrailBlazeUser: Button = findViewById(R.id.add_trailMates_user)
        addTrailBlazeUser.setOnClickListener {
            showAddFriendsDialog()
        }

        recyclerView = findViewById(R.id.partyMemberRecyclerView)
        userAdapter = UserAdapter(this, userList)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = userAdapter
        addCurrentUserToParty()
        updatePartyMemberCount()
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
                    friendsList.add(AddFriend(username, false)) // Add friend to the list
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

    private fun showTimerDialog() {
        // Inflate the custom layout
        val dialogView = layoutInflater.inflate(R.layout.dialog_timer, null)

        // Create the dialog
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)

        val dialog = dialogBuilder.create()

        // Initialize the timer TextView
        val timerTextView: TextView = dialogView.findViewById(R.id.timer_text_view)
        val startButton: Button = dialogView.findViewById(R.id.start_button)
        val stopButton: Button = dialogView.findViewById(R.id.stop_button)
        val saveTimeButton: Button = dialogView.findViewById(R.id.save_time_button)
        val notifyWatchersButton: Button = dialogView.findViewById(R.id.notify_watchers_button)
        val emergencyButton: Button = dialogView.findViewById(R.id.emergency_button)

        var handler = Handler()
        var isRunning = false
        var startTime = 0L
        var elapsedTime = 0L

        // Runnable to update the timer
        val updateTimer: Runnable = object : Runnable {
            override fun run() {
                if (isRunning) {
                    elapsedTime = System.currentTimeMillis() - startTime
                    val seconds = (elapsedTime / 1000) % 60
                    val minutes = (elapsedTime / (1000 * 60)) % 60
                    val hours = (elapsedTime / (1000 *60* 60)) % 24
                    timerTextView.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                    handler.postDelayed(this, 1000) // Update every second
                }
            }
        }

        // Start button click listener
        startButton.setOnClickListener {
            if (!isRunning) {
                startTime = System.currentTimeMillis() - elapsedTime // Resume from the last elapsed time
                isRunning = true
                handler.post(updateTimer) // Start updating the timer
            }
        }

        // Stop button click listener
        stopButton.setOnClickListener {
            isRunning = false // Pause the timer
        }

        saveTimeButton.setOnClickListener {

            isRunning = false // Pause the timer

            // Get the elapsed time as a String
            val elapsedTimeString = timerTextView.text.toString()

            // Create a TimeRecord object with parkImageUrl
            val timeRecord = TimeRecord(parkName, elapsedTimeString, parkImageUrl, parkCode)

            // Save the record to Firestore
            saveTimeToFirestore(timeRecord)

            showConfetti()
            Toast.makeText(this, "Time saved: $elapsedTimeString for park: $parkName", Toast.LENGTH_SHORT).show()
        }

        // Notify watchers button click listener
        notifyWatchersButton.setOnClickListener {
            // Logic to notify watchers
            Toast.makeText(this, "Notifying watchers...", Toast.LENGTH_SHORT).show()
        }

        // Emergency button click listener
        emergencyButton.setOnClickListener {
            showEmergencyDialog()
        }

        // Show the dialog
        dialog.show()
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

    private fun saveTimeToFirestore(timeRecord: TimeRecord) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            // Reference to the user's document in Firestore
            val userDocRef = firestore.collection("users").document(userId)

            // Update the timeRecords field, creating it if it doesn't exist
            userDocRef.update("timeRecords", FieldValue.arrayUnion(timeRecord))
                .addOnSuccessListener {
                    // After saving the time record, delete party members
                    deletePartyMembers(userId)
                    // Notify watchers
                    //notifyWatchers(userId)
                    achievementManager.checkAndGrantConquerorBadge()
                    achievementManager.saveBadgeToUserProfile("conqueror")
                    Log.d("AttemptTrailActivity", "Time record saved successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("AttemptTrailActivity", "Error saving time record: ${e.message}")
                }
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    // Method to show the emergency confirmation dialog
    private fun showEmergencyDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Emergency Call")
        builder.setMessage("Are you sure you want to call 911?")
        builder.setPositiveButton("Yes") { dialog, which ->
            callEmergencyNumber()
        }
        builder.setNegativeButton("No") { dialog, which ->
            dialog.dismiss()
        }
        builder.show()
    }

    // Method to initiate the emergency call
    private fun callEmergencyNumber() {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:911")
        startActivity(intent)
    }

    private fun fetchParkDetails(parkCode: String) {
        RetrofitInstance.api.getParkDetails(parkCode).enqueue(object : Callback<NPSResponse> {
            override fun onResponse(call: Call<NPSResponse>, response: Response<NPSResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { npsResponse ->
                        val park = npsResponse.data.firstOrNull() // Assuming the first park in the response
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
                }
                .addOnFailureListener { e ->
                    Log.e("AttemptTrailActivity", "Error saving party members: ${e.message}")
                }
        } else {
            Log.e("AttemptTrailActivity", "User not authenticated")
        }
    }

    private fun deletePartyMembers(userId: String) {
        val userDocRef = firestore.collection("users").document(userId)

        // Remove party members by deleting the "partyMembers" field
        userDocRef.update("partyMembers", FieldValue.delete())
            .addOnSuccessListener {
                Log.d("AttemptTrailActivity", "Party members deleted successfully")
            }
            .addOnFailureListener { e ->
                Log.e("AttemptTrailActivity", "Error deleting party members: ${e.message}")
            }
    }
}
