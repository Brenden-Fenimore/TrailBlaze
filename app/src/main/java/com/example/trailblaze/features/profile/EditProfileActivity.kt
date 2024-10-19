package com.example.trailblaze.features.profile

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.trailblaze.R

class EditProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        findViewById<ImageButton>(R.id.chevron_left).setOnClickListener{
            onBackPressedDispatcher.onBackPressed()
        }
        }
    }

