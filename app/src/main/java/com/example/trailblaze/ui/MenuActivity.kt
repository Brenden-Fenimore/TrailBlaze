package com.example.trailblaze.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.trailblaze.R
import android.widget.ImageButton
import com.example.trailblaze.login.LoginActivity
import com.example.trailblaze.settings.SettingsScreenActivity
import com.example.trailblaze.ui.Map.MapFragment
import com.example.trailblaze.ui.profile.ProfileFragment

// Activity for the menu screen
class MenuActivity : AppCompatActivity() {

    // Called when the activity is created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)          // Set the layout for this activity to activity_menu.xml
        findViewById<ImageButton>(R.id.chevron_left).setOnClickListener{        // Find the ImageButton by its ID and set a click listener
            onBackPressedDispatcher.onBackPressed()     // When the button is clicked, go back to the previous activity
        }

        // Handle settings button press and navigate to SettingsScreenActivity
        findViewById<ImageButton>(R.id.imageButton).setOnClickListener {
            val intent = Intent(this, SettingsScreenActivity::class.java)
            startActivity(intent)
        }

        val logoutButton = findViewById<Button>(R.id.logoutbtn)
        logoutButton.setOnClickListener {
            handleLogout()
        }

        // Initialize the mapButton
        val mapButton = findViewById<ImageButton>(R.id.mapButton)

        // Set the click listener for the mapButton
        mapButton.setOnClickListener {
            val fragment = MapFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        // Profile button -> Navigate to ProfileFragment
        val profileButton = findViewById<ImageButton>(R.id.profileButton)
        profileButton.setOnClickListener {
            val fragment = ProfileFragment()  // Replace with actual fragment
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }


    }

    // Helper function to handle logout logic
    private fun handleLogout() {
        val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isLoggedIn", false)
        editor.apply()

        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
}
