package com.example.trailblaze

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.trailblaze.databinding.ActivityMainBinding
import com.example.trailblaze.login.LoginActivity
import com.example.trailblaze.ui.UserPreferences.UserPreferencesManager
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var userPreferencesManager: UserPreferencesManager
    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //hide the ActionBar
        supportActionBar?.hide()

        val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        //if user is not logged in take them to login screen
        if (!isLoggedIn) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Passing each menu ID as a set of Ids because each
            // menu should be considered as top level destinations.
            val navView: BottomNavigationView = binding.navView

            val navController = findNavController(R.id.nav_host_fragment_activity_main)
            // Passing each menu ID as a set of Ids because each
            // menu should be considered as top level destinations.
            navView.setupWithNavController(navController)

            // Initialize
            firestore = FirebaseFirestore.getInstance()

            userPreferencesManager = UserPreferencesManager()

            // Fetch user preferences
            userPreferencesManager.fetchUserPreferences(
                onSuccess = { userPreferences ->
                    // Get the preferred distance
                    val preferredDistance = userPreferences.distance ?: 10.0
                },
                onFailure = { errorMessage ->
                    // Handle the error (e.g., show a toast or log it)
                    println("Error fetching user preferences: $errorMessage")
                }
            )

            //check if there is an intent to load a specific fragment
            if (savedInstanceState == null) {
                val fragmentToLoad = intent.getStringExtra("fragment_to_load")
                if (fragmentToLoad != null) {
                    when (fragmentToLoad) {
                        "EditProfileFragment" -> {
                            navController.navigate(R.id.editProfileFragment)
                        }
                    }
                }
            }
        }
    }
}
