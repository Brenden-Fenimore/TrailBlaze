package com.example.trailblaze.settings

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.trailblaze.R

class PrivacyAndSecurityActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_and_security)

        //set the listener for the back button
        findViewById<ImageButton>(R.id.chevron_left).setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
        }
    }
}