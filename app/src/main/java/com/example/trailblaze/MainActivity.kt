package com.example.trailblaze

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.trailblaze.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //hide the ActionBar
        supportActionBar?.hide()

            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Passing each menu ID as a set of Ids because each
            // menu should be considered as top level destinations.
            val navView: BottomNavigationView = binding.navView

            val navController = findNavController(R.id.nav_host_fragment_activity_main)
            // Passing each menu ID as a set of Ids because each
            // menu should be considered as top level destinations.
            navView.setupWithNavController(navController)

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
