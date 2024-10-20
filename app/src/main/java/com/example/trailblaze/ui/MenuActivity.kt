package com.example.trailblaze.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.trailblaze.R
import android.widget.ImageButton
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.trailblaze.databinding.ActivityMenuBinding
import com.example.trailblaze.login.LoginActivity
import com.example.trailblaze.settings.ContactUsActivity
import com.example.trailblaze.settings.SafetyActivity
import com.example.trailblaze.settings.SettingsScreenActivity
import com.example.trailblaze.settings.SupportScreenActivity
import com.example.trailblaze.ui.achievements.AchievementManager
import com.example.trailblaze.ui.achievements.AchievementsActivity
import com.example.trailblaze.ui.home.HomeFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MenuActivity : AppCompatActivity() {

    private lateinit var achievementManager: AchievementManager
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize AchievementManager
        achievementManager = AchievementManager(this)

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        loadProfilePicture()

        val logoutbtn = findViewById<Button>(R.id.logoutbtn)


        //set click listener for the logout button
        logoutbtn.setOnClickListener {
            val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean("isLoggedIn", false)
            editor.apply()

            //navigate back to login
            val intent = Intent(this, LoginActivity::class.java)

            //clear the stack
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }

        binding.chevronLeft.setOnClickListener { onBackPressed() }

        binding.settingsbtn.setOnClickListener {
            val intent = Intent(this, SettingsScreenActivity::class.java)
            startActivity(intent)
        }


        binding.navigationHome.setOnClickListener {
            val intent = Intent(this, HomeFragment::class.java)
            startActivity(intent)
        }

        binding.navigationHelp.setOnClickListener {
            val intent = Intent(this, SupportScreenActivity::class.java)
            startActivity(intent)
        }

        binding.navigationContact.setOnClickListener {
            val intent = Intent(this, ContactUsActivity::class.java)
            startActivity(intent)
        }

        binding.navigationSafety.setOnClickListener {
            // Grant the Safety Expert badge
            achievementManager.checkAndGrantSafetyExpertBadge()

            // Save to Firebase
            achievementManager.saveBadgeToUserProfile("safetyexpert")

            val intent = Intent(this, SafetyActivity::class.java)
            startActivity(intent)
        }

        binding.navigationTrailChallenges.setOnClickListener {
            val intent = Intent(this, AchievementsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadProfilePicture() {
        val userId = auth.currentUser?.uid ?: return
        val userRef = firestore.collection("users").document(userId)

        userRef.get().addOnSuccessListener { document ->
            if (document != null) {
                val imageUrl = document.getString("profileImageUrl")
                if (imageUrl != null) {
                    // Load the image URL into the ImageView using Glide
                    Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.account_circle) // Placeholder image while loading
                        .error(R.drawable.account_circle) // Error image if the load fails
                        .into(binding.profilePicture) // Assuming you have an ImageView with this ID in your layout
                } else {
                    // Handle the case where the URL is missing
                    binding.profilePicture.setImageResource(R.drawable.account_circle) // Set a default image
                }
            }
        }.addOnFailureListener { exception ->
            Log.e("Firestore", "Error getting profile picture: ${exception.message}")
        }
    }
}
