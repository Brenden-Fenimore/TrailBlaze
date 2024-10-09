package com.example.trailblaze.settings

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.trailblaze.R


class SettingsScreenActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //initialize views
        val toAccount = findViewById<ImageButton>(R.id.toAccount)
        val toNotification = findViewById<ImageButton>(R.id.toNotifications)
        val toAppearance = findViewById<ImageButton>(R.id.toAppearance)
        val toPrivacy = findViewById<ImageButton>(R.id.toPrivacy)
        val toHelpandSupport = findViewById<ImageButton>(R.id.toHelpandSupport)
        val toAbout = findViewById<ImageButton>(R.id.toAbout)



        //set the listener for the back button
        findViewById<ImageButton>(R.id.chevron_left).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        }
    }