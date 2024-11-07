package com.example.trailblaze

import android.content.Intent
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.trailblaze.databinding.ActivityMainBinding
import com.example.trailblaze.login.LoginActivity
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide the ActionBar
        supportActionBar?.hide()

        // Check login status
        val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        // If user is not logged in, take them to login screen
        if (!isLoggedIn) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // Inflate activity layout
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Set up BottomNavigationView
            val navView: BottomNavigationView = binding.navView
            val navController = findNavController(R.id.nav_host_fragment_activity_main)
            navView.setupWithNavController(navController)

            // Initialize Firestore
            firestore = FirebaseFirestore.getInstance()
            
        }
    }
}