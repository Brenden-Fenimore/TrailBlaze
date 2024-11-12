package com.example.trailblaze.watcherFeature

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.trailblaze.R
import com.example.trailblaze.databinding.ActivityWatcherProfileBinding

class WatcherProfile : AppCompatActivity() {

    private lateinit var binding: ActivityWatcherProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
binding = ActivityWatcherProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            onBackPressed() }
    }
}