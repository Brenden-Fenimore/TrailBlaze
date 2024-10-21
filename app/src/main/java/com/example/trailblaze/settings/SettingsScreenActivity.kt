package com.example.trailblaze.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.trailblaze.R
import com.example.trailblaze.login.LoginActivity
import androidx.navigation.findNavController
import com.example.trailblaze.ui.home.HomeFragment


class SettingsScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_screen)

        //initialize views
        val toAccount = findViewById<ImageButton>(R.id.toAccount)
        val toNotification = findViewById<ImageButton>(R.id.toNotifications)
        val toAppearance = findViewById<ImageButton>(R.id.toAppearance)
        val toPrivacy = findViewById<ImageButton>(R.id.toPrivacy)
        val toHelpAndSupport = findViewById<ImageButton>(R.id.toHelpandSupport)
        val toAbout = findViewById<ImageButton>(R.id.toAbout)
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

        //set the listener for the back button
        findViewById<ImageButton>(R.id.chevron_left).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        //set the listener for the help and support button
        toHelpAndSupport.setOnClickListener {
            val intent = Intent(this, SupportScreenActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        //set click listener for notification
        toNotification.setOnClickListener {
            val intent = Intent(this, NotificationsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        //set click listener for Privacy and security
        toPrivacy.setOnClickListener {
            val intent = Intent(this, PrivacyAndSecurityActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        //set click listener for about
        toAbout.setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        //set click listener for appearances
        toAppearance.setOnClickListener {
            val intent = Intent(this, AppearanceActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        // Set click listener for account
        toAccount.setOnClickListener {
            val navController = findNavController(R.id.mobile_navigation)
            navController.navigate(R.id.editProfileFragment)
        }
        
    }
}