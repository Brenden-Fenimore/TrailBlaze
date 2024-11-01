package com.example.trailblaze.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.trailblaze.R

class AppearanceActivity : AppCompatActivity() {
    private lateinit var darkModeSwitch: Switch
    private lateinit var metricUnitsSwitch: Switch // Declare the metric units switch
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appearance)
        supportActionBar?.hide()

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)

        // Check current dark mode setting and apply it
        val isDarkModeEnabled = sharedPreferences.getBoolean("isDarkModeEnabled", false)
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkModeEnabled) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )

        // Find views
        darkModeSwitch = findViewById(R.id.darkmodeSwitch) // Assuming you have the correct ID
        metricUnitsSwitch = findViewById(R.id.metricSwitch) // Assuming this is the ID for the metric switch

        // Set the initial state of the switches
        darkModeSwitch.isChecked = isDarkModeEnabled
        metricUnitsSwitch.isChecked = sharedPreferences.getBoolean("isMetricUnits", false)

        // Set the listener for the back button
        findViewById<ImageButton>(R.id.chevron_left).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Set the listener for the dark mode switch
        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Enable dark mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                saveDarkModePreference(true)
            } else {
                // Disable dark mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                saveDarkModePreference(false)
            }
        }

        // Set the listener for the metric units switch
        metricUnitsSwitch.setOnCheckedChangeListener { _, isChecked ->
            saveMetricUnitsPreference(isChecked)
            if (isChecked) {
                // Here you can update your UI to reflect the metric units
                Toast.makeText(this, "Metric units enabled", Toast.LENGTH_SHORT).show()
                // Call a method to update the UI where necessary
            } else {
                // Here you can update your UI to reflect the non-metric units
                Toast.makeText(this, "Imperial units enabled", Toast.LENGTH_SHORT).show()
                // Call a method to update the UI where necessary
            }
        }
    }

    private fun saveDarkModePreference(isEnabled: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("isDarkModeEnabled", isEnabled)
        editor.apply()
    }

    private fun saveMetricUnitsPreference(isEnabled: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("isMetricUnits", isEnabled)
        editor.apply()
    }
}