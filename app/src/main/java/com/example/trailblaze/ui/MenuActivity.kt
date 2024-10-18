package com.example.trailblaze.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.trailblaze.R
import android.widget.ImageButton
import android.widget.TextView
import com.example.trailblaze.login.LoginActivity
import com.example.trailblaze.settings.ContactUsActivity
import com.example.trailblaze.settings.SafetyActivity
import com.example.trailblaze.settings.SettingsScreenActivity
import com.example.trailblaze.settings.SupportScreenActivity
import com.example.trailblaze.ui.achievements.AchievementManager
import com.example.trailblaze.ui.achievements.AchievementsActivity
import com.example.trailblaze.ui.home.HomeFragment

class MenuActivity : AppCompatActivity() {

    private lateinit var achievementManager: AchievementManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        // Initialize AchievementManager
        achievementManager = AchievementManager(this)

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

        findViewById<ImageButton>(R.id.chevron_left).setOnClickListener{
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<ImageButton>(R.id.settingsbtn).setOnClickListener{
            val intent = Intent(this, SettingsScreenActivity::class.java)
            startActivity(intent)
        }


        findViewById<TextView>(R.id.navigation_home).setOnClickListener{
           val intent = Intent(this, HomeFragment::class.java)
            startActivity(intent)
        }

        findViewById<TextView>(R.id.navigation_help).setOnClickListener{
            val intent = Intent(this, SupportScreenActivity::class.java)
            startActivity(intent)
        }

        findViewById<TextView>(R.id.navigation_contact).setOnClickListener{
            val intent = Intent(this, ContactUsActivity::class.java)
            startActivity(intent)
        }

        findViewById<TextView>(R.id.navigation_safety).setOnClickListener{

            // Grant the Safety Expert badge
            achievementManager.checkAndGrantSafetyExpertBadge()

            //save to firebase
            achievementManager.saveBadgeToUserProfile("safetyexpert")

            val intent = Intent(this, SafetyActivity::class.java)
            startActivity(intent)
        }

        findViewById<TextView>(R.id.navigation_trailChallenges).setOnClickListener{
            val intent = Intent(this, AchievementsActivity::class.java)
            startActivity(intent)
        }
    }
}
