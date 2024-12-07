package com.byteforce.trailblaze

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.byteforce.trailblaze.login.LoginActivity
import com.google.android.libraries.places.api.Places
import com.google.firebase.firestore.FirebaseFirestore
import com.byteforce.trailblaze.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import android.Manifest
import com.byteforce.trailblaze.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Initialize notification helper
        notificationHelper = NotificationHelper(this)

        // Set up notifications when user is logged in
        FirebaseAuth.getInstance().currentUser?.let { user ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestNotificationPermission(user.uid)
            } else {
                notificationHelper.setupFirestoreListeners(user.uid)
            }
        }

        // Hide the ActionBar
        supportActionBar?.hide()
        // Initialize Places API
        Places.initialize(applicationContext, BuildConfig.PLACES_API_KEY)
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

            // Set up BottomNavigationView
            val navView: BottomNavigationView = binding.navView
            val navController = findNavController(R.id.nav_host_fragment_activity_main)
            navView.setupWithNavController(navController)

            // Initialize Firestore
            firestore = FirebaseFirestore.getInstance()

        }
    }

    private fun requestNotificationPermission(userId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED) {
                registerForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        notificationHelper.setupFirestoreListeners(userId)
                    }
                }.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                notificationHelper.setupFirestoreListeners(userId)
            }
        }
    }
}