package com.example.trailblaze.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.trailblaze.R
import com.example.trailblaze.login.LoginActivity


class SettingsScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_screen)

        val toNotification = findViewById<TextView>(R.id.notification)
        val toAppearance = findViewById<TextView>(R.id.appearance)
        val toPrivacy = findViewById<TextView>(R.id.privacyAndSecurity)
        val toAbout = findViewById<TextView>(R.id.about)
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

    }
}