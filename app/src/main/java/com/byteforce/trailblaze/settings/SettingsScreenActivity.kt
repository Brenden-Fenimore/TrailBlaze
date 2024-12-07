package com.byteforce.trailblaze.settings

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.byteforce.trailblaze.R
import com.byteforce.trailblaze.login.LoginActivity


// Activity that represents the settings screen for the application
class SettingsScreenActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences // SharedPreferences for storing user settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_screen) // Set the content view for the activity

        // Hide the ActionBar for a full-screen experience
        supportActionBar?.hide()

        // Initialize SharedPreferences to store user-related data
        sharedPreferences = getSharedPreferences("your_shared_preferences_name", MODE_PRIVATE)

        // Set click listener for the logout button
        val logoutbtn = findViewById<Button>(R.id.logoutbtn)
        logoutbtn.setOnClickListener {
            // Create an editor to modify the shared preferences
            val editor = sharedPreferences.edit()
            editor.putBoolean("isLoggedIn", false) // Update the login status to false
            editor.apply() // Apply the changes asynchronously

            // Create an intent to navigate back to the LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            // Clear the activity stack to prevent going back to the settings screen
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent) // Start the login activity
            finish() // Finish the current activity
        }

        // Set the listener for the back button to navigate back to the previous screen
        findViewById<ImageButton>(R.id.chevron_left).setOnClickListener {
            onBackPressedDispatcher.onBackPressed() // Handle back navigation
        }

        // Set click listeners for the TextViews to navigate to respective settings activities
        findViewById<TextView>(R.id.notification).setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java)) // Open NotificationsActivity
        }

        findViewById<TextView>(R.id.appearance).setOnClickListener {
            startActivity(Intent(this, AppearanceActivity::class.java)) // Open AppearanceActivity
        }

        findViewById<TextView>(R.id.privacyAndSecurity).setOnClickListener {
            startActivity(Intent(this, PrivacyAndSecurityActivity::class.java)) // Open PrivacyAndSecurityActivity
        }
    }
}