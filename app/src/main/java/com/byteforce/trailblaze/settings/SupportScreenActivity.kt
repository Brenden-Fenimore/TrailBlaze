package com.byteforce.trailblaze.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.byteforce.trailblaze.R


// Activity that provides support options to the user, such as contacting support and emergency calls
class SupportScreenActivity : AppCompatActivity() {

    // Declaration of UI components
    private lateinit var contactUsTxt: TextView // TextView for "Contact Us" option
    private lateinit var emergencyTxt: TextView // TextView for "Emergency" option

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_support_screen) // Set the content view for the activity

        // Hide the ActionBar for a clean interface
        supportActionBar?.hide()

        // Initialize the contactUsTxt variable by finding the TextView in the layout
        contactUsTxt = findViewById(R.id.contactUs)

        // Set click listener for the "Contact Us" TextView
        contactUsTxt.setOnClickListener {
            // Create an intent to navigate to the ContactUsActivity
            val intent = Intent(this, ContactUsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Set the new task flag
            startActivity(intent) // Start the ContactUsActivity
        }

        // Set the listener for the back button to handle back navigation
        findViewById<ImageButton>(R.id.chevron_left).setOnClickListener {
            onBackPressedDispatcher.onBackPressed() // Handle back navigation
        }

        // Initialize the emergencyTxt variable by finding the TextView in the layout
        emergencyTxt = findViewById(R.id.emergency)
        // Set click listener for the "Emergency" TextView
        emergencyTxt.setOnClickListener {
            showEmergencyDialog() // Show the emergency confirmation dialog
        }
    }

    // Function to display a dialog for confirming an emergency call
    private fun showEmergencyDialog() {
        val builder = AlertDialog.Builder(this) // Create an AlertDialog builder
        builder.setTitle("Emergency Call") // Set the title of the dialog
        builder.setMessage("Are you sure you want to call 911?") // Set the message for the dialog
        builder.setPositiveButton("Yes") { dialog, which ->
            callEmergencyNumber() // If "Yes" is clicked, call the emergency number
        }
        builder.setNegativeButton("No") { dialog, which ->
            dialog.dismiss() // If "No" is clicked, dismiss the dialog
        }
        builder.show() // Show the dialog to the user
    }

    // Function to initiate a call to the emergency number
    private fun callEmergencyNumber() {
        val intent = Intent(Intent.ACTION_DIAL) // Create an intent to dial a number
        intent.data = Uri.parse("tel:911") // Set the data for the intent to the emergency number
        startActivity(intent) // Start the dial activity
    }
}