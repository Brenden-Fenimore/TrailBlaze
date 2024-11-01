package com.example.trailblaze.ui.parks

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.trailblaze.R
import com.example.trailblaze.nps.NPSResponse
import com.example.trailblaze.nps.Park
import com.example.trailblaze.nps.RetrofitInstance
import com.example.trailblaze.ui.achievements.AchievementManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AttemptTrailActivity : AppCompatActivity() {

    private lateinit var parkCode: String
    private lateinit var parkName: String // Store the park name
    private lateinit var parkNameTextView: TextView
    private lateinit var soloJourneyCheckBox: CheckBox
    private lateinit var partyInfoLayout: LinearLayout
    private lateinit var startTrailButton: Button
    private lateinit var firestore: FirebaseFirestore
    private lateinit var achievementManager: AchievementManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attempt_trail)

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
        }
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

        // Save time button click listener
        saveTimeButton.setOnClickListener {
            // Get the elapsed time as a String
            val elapsedTimeString = timerTextView.text.toString()

            // Create a TimeRecord object
            val timeRecord = TimeRecord(parkName, elapsedTimeString)

            // Save the record to Firestore
            saveTimeToFirestore(timeRecord)
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

    private fun saveTimeToFirestore(timeRecord: TimeRecord) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            // Reference to the user's document in Firestore
            val userDocRef = firestore.collection("users").document(userId)

            // Update the timeRecords field, creating it if it doesn't exist
            userDocRef.update("timeRecords", FieldValue.arrayUnion(timeRecord))
                .addOnSuccessListener {
                    achievementManager.checkAndGrantConquerorBadge()

                    // Save to Firebase
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
    }
}
