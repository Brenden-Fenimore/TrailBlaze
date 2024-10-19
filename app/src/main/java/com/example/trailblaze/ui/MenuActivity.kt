package com.example.trailblaze.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.trailblaze.R
import android.widget.ImageButton

class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        findViewById<ImageButton>(R.id.chevron_left).setOnClickListener{
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
