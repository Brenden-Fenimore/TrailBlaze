package com.example.trailblaze.settings

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.trailblaze.R
import com.example.trailblaze.login.LoginActivity


class SettingsScreenActivity : AppCompatActivity() {

    private lateinit var darkModeSwitch: Switch
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_screen)
        // Hide the ActionBar
        supportActionBar?.hide()

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)

        // Check current dark mode setting and apply it
        val isDarkModeEnabled = sharedPreferences.getBoolean("isDarkModeEnabled", false)
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkModeEnabled) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )

        // Find views
        darkModeSwitch = findViewById(R.id.leaderboard_switch) // Assuming you have the correct ID
        val logoutbtn = findViewById<Button>(R.id.logoutbtn)

        // Set the initial state of the switch
        darkModeSwitch.isChecked = isDarkModeEnabled

        // Set click listener for the logout button
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

        // Set the listener for the dark mode switch
        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Enable dark mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                saveDarkModePreference(true)
            } else {
                // Disable dark mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                saveDarkModePreference(false)
            }
        }
    }

    private fun saveDarkModePreference(isEnabled: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("isDarkModeEnabled", isEnabled)
        editor.apply()
    }
}