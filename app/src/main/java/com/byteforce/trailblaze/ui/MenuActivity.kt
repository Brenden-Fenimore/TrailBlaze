package com.byteforce.trailblaze.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.byteforce.trailblaze.R
import com.byteforce.trailblaze.databinding.ActivityMenuBinding
import com.byteforce.trailblaze.firestore.ImageLoader
import com.byteforce.trailblaze.firestore.UserRepository
import com.byteforce.trailblaze.login.LoginActivity
import com.byteforce.trailblaze.settings.AboutActivity
import com.byteforce.trailblaze.settings.SafetyActivity
import com.byteforce.trailblaze.settings.SettingsScreenActivity
import com.byteforce.trailblaze.settings.SupportScreenActivity
import com.byteforce.trailblaze.ui.achievements.AchievementManager
import com.byteforce.trailblaze.ui.achievements.AchievementsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class MenuActivity : AppCompatActivity() {

    private lateinit var achievementManager: AchievementManager
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMenuBinding
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Hide the ActionBar
        supportActionBar?.hide()

        val toAbout = findViewById<TextView>(R.id.about)

        // Initialize AchievementManager
        achievementManager = AchievementManager(this)

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        userRepository = UserRepository(firestore)
        loadProfilePicture()

        val logoutbtn = findViewById<Button>(R.id.logoutbtn)

        // Set click listener for the logout button
        logoutbtn.setOnClickListener {
            val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
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

        binding.chevronLeft.setOnClickListener { onBackPressed() }

        binding.settingsbtn.setOnClickListener {
            val intent = Intent(this, SettingsScreenActivity::class.java)
            startActivity(intent)
        }

        binding.navigationHelp.setOnClickListener {
            val intent = Intent(this, SupportScreenActivity::class.java)
            startActivity(intent)
        }

        binding.navigationSafety.setOnClickListener {
            // Get the current user's ID
            val userId = auth.currentUser?.uid
            if (userId != null) {
                // Grant the Safety Expert badge
                achievementManager.checkAndGrantSafetyExpertBadge(userId)
            } else {
                Log.e("MenuActivity", "User not authenticated.")
            }

            val intent = Intent(this, SafetyActivity::class.java)
            startActivity(intent)
        }

        binding.navigationTrailChallenges.setOnClickListener {
            val intent = Intent(this, AchievementsActivity::class.java)
            startActivity(intent)
        }

        // Set click listener for about
        toAbout.setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    private fun loadProfilePicture() {
        val userId = auth.currentUser?.uid ?: return
        userRepository.getUserProfileImage(userId) { imageUrl ->
            ImageLoader.loadProfilePicture(this, binding.profilePicture, imageUrl)
        }
    }
}