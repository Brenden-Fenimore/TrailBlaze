package com.example.trailblaze.ui.parks

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.viewModels
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
import java.text.SimpleDateFormat
import java.util.*
import com.example.trailblaze.firestore.UserManager
import com.example.trailblaze.firestore.User
import com.example.trailblaze.watcherFeature.WatcherMemberViewModel
import com.google.firebase.firestore.SetOptions

class TimerActivity: AppCompatActivity() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var achievementManager: AchievementManager
    private lateinit var parkCode: String
    private lateinit var parkName: String // Store the park name
    private lateinit var parkImageUrl: String
    private lateinit var parkNameTextView: TextView
    private lateinit var activities: Array<String>
    private lateinit var partyMembers: Array<String>
    private lateinit var userAdapter: UserAdapter

    // Property to hold the current user
    private var currentUser: User? = null

    // Watcher List
private val selectedWatchers = mutableListOf<AddFriend>()

    // Initialize WatcherMemberViewModel

   private val watcherMemberViewModel: WatcherMemberViewModel by viewModels ()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)
        supportActionBar?.hide()
        currentUser = UserManager.getCurrentUser()

        firestore = FirebaseFirestore.getInstance()
        // Get the park code from the intent
        parkCode = intent.getStringExtra("PARK_CODE") ?: ""
        parkNameTextView = findViewById(R.id.parkNameTextView)
        fetchParkDetails(parkCode)
        activities = intent.getStringArrayExtra("PARK_ACTIVITIES") ?: arrayOf()

        partyMembers = intent.getStringArrayExtra("PARTY_MEMBERS") ?: arrayOf()



        // Log the activities to see what was pulled in
        Log.d("AttemptTrailActivity", "Activities received: ${activities.joinToString(", ")}")


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


        // Initialize the timer TextView
        val timerTextView: TextView = findViewById(R.id.timer_text_view)
        val startButton: Button = findViewById(R.id.start_button)
        val stopButton: Button = findViewById(R.id.stop_button)
        val saveTimeButton: Button = findViewById(R.id.save_time_button)
        val notifyWatchersButton: Button = findViewById(R.id.notify_watchers_button)
        val emergencyButton: Button = findViewById(R.id.emergency_button)

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
                    val hours = (elapsedTime / (1000 * 60 * 60)) % 24
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

            // Get the current date as a formatted string
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            // Create a TimeRecord object with the current timestamp and date
            val timeRecord = TimeRecord(
                parkName,
                elapsedTimeString,
                parkImageUrl,
                parkCode,
                System.currentTimeMillis(), // current timestamp
                currentDate // current date
            )

            // Check if "Rock Climbing" is in the activities
            if (activities.contains("Hiking")) {
                achievementManager.checkAndGrantMountainClimberBadge()
            }

            // Check if "Rock Climbing" is in the activities
            if (activities.contains("Wildlife Watching")) {
                achievementManager.checkAndGrantWildlifeBadge()
            }
            // Save the record to Firestore, passing the elapsed time in milliseconds
            saveTimeToFirestore(timeRecord, convertElapsedTimeToMillis(elapsedTimeString))

            showConfetti()
            Toast.makeText(this, "Time saved: $elapsedTimeString for park: $parkName", Toast.LENGTH_SHORT).show()
        }

        // Notify watchers button click listener
        notifyWatchersButton.setOnClickListener {
          showWatcherSelectionDialog() // Start Watcher Selection Dialog
        }

        // Emergency button click listener
        emergencyButton.setOnClickListener {
            showEmergencyDialog()
        }
    }

    private fun showWatcherSelectionDialog() {
        watcherMemberViewModel.fetchWatcherMembers()

        watcherMemberViewModel.watcherMembers.observe(this) { watcherList ->
            val watcherNames = watcherList.map { it.username }.toTypedArray()
            val selectedItems = BooleanArray(watcherNames.size)

          val dialog =  AlertDialog.Builder(this)
                .setTitle("Select Watchers")
                .setMultiChoiceItems(watcherNames, selectedItems) { _, index, isSelected ->
                    val friend = watcherList[index]
                    if (isSelected) {
                        watcherMemberViewModel.addWatcher(friend)
                    } else {
                        watcherMemberViewModel.removeWatcher(friend)
                    }
                }
                .setPositiveButton("NOTIFY") { _, _ ->
                    watcherMemberViewModel.selectedWatchers.value?.forEach { watcher ->
                        sendNotificationToFriend(watcher.userId)
                    }

                }
                .setNegativeButton("CANCEL") {dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
                .create()
            dialog.show()
        }
    }

  private fun sendNotificationToFriend(friendId: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Reference to the current user's document in Firestore
        val currentUserRef = firestore.collection("users").document(currentUserId)

        // Get current user's name from Firestore
        currentUserRef.get().addOnSuccessListener { userDocument ->
            val currentUserName = userDocument.getString("username") ?: "User"

            // Reference to the friend’s document in Firestore
            val friendRef = firestore.collection("users").document(friendId)

            friendRef.get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val pendingNotifications = document.get("pendingNotifications") as? List<String> ?: emptyList()

                    // Construct the notification message using the party members' names
                    // Assuming 'partyMembers' contains names directly:
                    val partyMembersString = partyMembers.joinToString(", ") // Join party members' names with commas
                    // Construct the notification message including the park name
                    val notificationMessage = "$currentUserName has just embarked on a new trail adventure at $parkName! Please keep an eye on them and check in periodically. Their safety is important."
                    // Check if the notification message has already been sent
                    if (pendingNotifications.contains(notificationMessage)) {
                        Toast.makeText(this, "Notification already sent to this friend.", Toast.LENGTH_SHORT).show()
                    } else {
                        // Update the friend's document to add the notification message to pendingNotifications
                        val notificationUpdates = hashMapOf<String, Any>(
                            "pendingNotifications" to FieldValue.arrayUnion(notificationMessage)
                        )

                        friendRef.set(notificationUpdates, SetOptions.merge())
                            .addOnSuccessListener {
                                Toast.makeText(this, "Notification sent successfully!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { exception ->
                                Log.e("TimerActivity", "Error sending notification: ", exception)
                                Toast.makeText(this, "Failed to send notification.", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Log.e("TimerActivity", "Friend document does not exist")
                    Toast.makeText(this, "User not found.", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { exception ->
                Log.e("TimerActivity", "Error fetching friend document: ", exception)
                Toast.makeText(this, "Error fetching user data.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Log.e("TimerActivity", "Error fetching current user document: ", exception)
            Toast.makeText(this, "Error fetching current user data.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun convertElapsedTimeToMillis(elapsedTime: String): Long {
        val timeParts = elapsedTime.split(":")
        var milliseconds = 0L

        // Ensure the elapsedTime is in the correct format
        if (timeParts.size == 3) {
            val hours = timeParts[0].toLongOrNull() ?: 0
            val minutes = timeParts[1].toLongOrNull() ?: 0
            val seconds = timeParts[2].toLongOrNull() ?: 0

            // Convert the time to milliseconds
            milliseconds = (hours * 3600 + minutes * 60 + seconds) * 1000
        }

        return milliseconds
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

    private fun saveTimeToFirestore(timeRecord: TimeRecord, elapsedTime: Long) {
        // Use UserManager to get the current user
        val currentUser = UserManager.getCurrentUser()
        val userId = currentUser?.uid

        if (userId != null) {
            // Reference to the user's document in Firestore
            val userDocRef = firestore.collection("users").document(userId)

            // Create a TimeRecord with the current timestamp
            val timeRecordWithTimestamp = timeRecord.copy(timestamp = System.currentTimeMillis())

            // Update the timeRecords field, creating it if it doesn't exist
            userDocRef.update("timeRecords", FieldValue.arrayUnion(timeRecordWithTimestamp))
                .addOnSuccessListener {
                    // After saving the time record, delete party members
                    deletePartyMembers(userId)
                    // Notify watchers
                    // notifyWatchers(userId)

                    // Check and grant the Explorer badge for evening completion
                    achievementManager.checkAndGrantExplorerBadge(timeRecordWithTimestamp.timestamp)

                    achievementManager.checkAndGrantConquerorBadge()

                    achievementManager.checkAndGrantTrailBlazerBadge()

                    // Check for Long Distance badge (5 minutes = 300 seconds)
                    if (elapsedTime > 300_000) { // 300,000 milliseconds = 5 minutes
                        achievementManager.checkAndGrantLongDistanceBadge()
                    }
                    // Check for Habitual Hiker badge
                    achievementManager.checkAndGrantHabitualBadge()

                    // Check for Weekend Warrior badge
                    achievementManager.checkAndGrantWeekendBadge(timeRecordWithTimestamp.timestamp)

                    // Check and grant the Daily Adventurer badge
                    achievementManager.checkForDailyAdventurerBadge(userId)
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
                        val park = npsResponse.data.firstOrNull()
                        park?.let {
                            populateParkDetails(it) // Pass the park object to populate the UI
                        } ?: run {
                            Toast.makeText(this@TimerActivity, "Park details not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<NPSResponse>, t: Throwable) {
                Toast.makeText(this@TimerActivity, "Error fetching park details: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun populateParkDetails(park: Park) {
        // Populate UI elements with park details
        parkNameTextView.text = park.fullName
        parkName = park.fullName // Store the park name
        parkImageUrl = park.images.firstOrNull()?.url ?: ""
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

    // Function to fetch favorite friends for the currently logged-in user
    private fun fetchFavoriteFriends(onComplete: (List<AddFriend>) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val friendsIds = document.get("favoriteFriends") as? List<String> ?: emptyList()
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
                    friendsList.add(AddFriend(username, friendId, false)) // Add friend to the list with id
                }
            })
        }

        // Wait until all friend data is fetched
        Tasks.whenAllSuccess<DocumentSnapshot>(tasks).addOnSuccessListener {
            onComplete(friendsList) // Return the populated friends list
        }
    }
}