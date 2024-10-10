package com.example.trailblaze.profile

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.trailblaze.R


class ProfileScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_screen)

        // Handle button clicks, profile updates, etc.
        val editButton = findViewById<ImageView>(R.id.editIcon)
        editButton.setOnClickListener {
            // Code to edit the profile here
        }

        val backButton = findViewById<ImageView>(R.id.backArrow)
        backButton.setOnClickListener {
            // Code to navigate back
            onBackPressed()
        }
    }
}