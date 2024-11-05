package com.example.trailblaze.settings

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.trailblaze.R
import com.example.trailblaze.login.LoginActivity
import com.example.trailblaze.login.TermsActivity


class SettingsScreenActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_screen)

        // Hide the ActionBar
        supportActionBar?.hide()

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("your_shared_preferences_name", MODE_PRIVATE)

        // Set click listener for the logout button
        val logoutbtn = findViewById<Button>(R.id.logoutbtn)
        logoutbtn.setOnClickListener {
            val editor = sharedPreferences.edit()
            editor.putBoolean("isLoggedIn", false)
            editor.apply()

            // Navigate back to login
            val intent = Intent(this, LoginActivity::class.java)
            // Clear the stack
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }

        // Set the listener for the back button
        findViewById<ImageButton>(R.id.chevron_left).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Set click listeners for the TextViews
        findViewById<TextView>(R.id.notification).setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        findViewById<TextView>(R.id.appearance).setOnClickListener {
            startActivity(Intent(this, AppearanceActivity::class.java))
        }

        findViewById<TextView>(R.id.privacyAndSecurity).setOnClickListener {
            startActivity(Intent(this, PrivacyAndSecurityActivity::class.java))
        }

    }
}