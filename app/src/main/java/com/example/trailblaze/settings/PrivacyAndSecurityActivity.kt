package com.example.trailblaze.settings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.trailblaze.R
import com.example.trailblaze.login.TermsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Activity to manage privacy and security settings for the user
class PrivacyAndSecurityActivity : AppCompatActivity() {
    private lateinit var dprivateSwitch: Switch // Switch to toggle private account setting
    private lateinit var auth: FirebaseAuth // Firebase Authentication instance
    private lateinit var firestore: FirebaseFirestore // Firestore instance for database operations

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_and_security) // Set the content view for the activity
        supportActionBar?.hide() // Hide the action bar for a more immersive experience

        // Initialize Firebase Auth and Firestore instances
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Find the switch in the layout
        dprivateSwitch = findViewById(R.id.dprivateSwitch)

        // Load the current privacy setting from Firestore
        loadPrivacySetting()

        // Set the listener for the switch to save the new privacy setting when toggled
        dprivateSwitch.setOnCheckedChangeListener { _, isChecked ->
            savePrivacySetting(isChecked) // Save the new privacy setting
        }

        // Set the listener for the back button to navigate back to the previous activity
        findViewById<ImageButton>(R.id.chevron_left).setOnClickListener {
            onBackPressedDispatcher.onBackPressed() // Handle back navigation
        }

        // Set the listener for the terms and conditions text to open the TermsActivity
        findViewById<TextView>(R.id.termsAndCon).setOnClickListener {
            startActivity(Intent(this, TermsActivity::class.java)) // Start TermsActivity
        }
    }

    // Function to load the current privacy setting from Firestore
    private fun loadPrivacySetting() {
        val userId = auth.currentUser?.uid ?: return // Get the current user's ID, return if null
        firestore.collection("users").document(userId).get() // Fetch the user's document from Firestore
            .addOnSuccessListener { document ->
                // Check if the document exists
                if (document != null && document.exists()) {
                    val isPrivateAccount = document.getBoolean("isPrivateAccount") ?: false // Get the privacy setting
                    dprivateSwitch.isChecked = isPrivateAccount // Update the switch's checked state
                }
            }
            .addOnFailureListener { exception ->
                // Log an error message if fetching the privacy setting fails
                Log.e("PrivacyAndSecurityActivity", "Error fetching privacy setting: ", exception)
            }
    }

    // Function to save the updated privacy setting to Firestore
    private fun savePrivacySetting(isPrivate: Boolean) {
        val userId = auth.currentUser?.uid ?: return // Get the current user's ID, return if null
        val userRef = firestore.collection("users").document(userId) // Reference to the user's document

        // Update the privacy setting in Firestore
        userRef.update("isPrivateAccount", isPrivate)
            .addOnSuccessListener {
                // Log success message if the update is successful
                Log.d("PrivacyAndSecurityActivity", "Privacy setting updated successfully.")
            }
            .addOnFailureListener { exception ->
                // Log an error message if updating the privacy setting fails
                Log.e("PrivacyAndSecurityActivity", "Error updating privacy setting: ", exception)
            }
    }
}