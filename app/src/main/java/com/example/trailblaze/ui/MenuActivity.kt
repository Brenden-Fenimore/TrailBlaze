package com.example.trailblaze.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.trailblaze.R
import android.widget.ImageButton

// Activity for the menu screen
class MenuActivity : AppCompatActivity() {

    // Called when the activity is created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)          // Set the layout for this activity to activity_menu.xml
        findViewById<ImageButton>(R.id.chevron_left).setOnClickListener{        // Find the ImageButton by its ID and set a click listener
            onBackPressedDispatcher.onBackPressed()     // When the button is clicked, go back to the previous activity
        }
    }

}
