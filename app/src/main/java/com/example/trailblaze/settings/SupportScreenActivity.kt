package com.example.trailblaze.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.trailblaze.R


class SupportScreenActivity : AppCompatActivity() {

    //declaration
    private lateinit var contactUsTxt: TextView
    private lateinit var bugtxt : TextView
    private lateinit var emergencyTxt : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_support_screen)
        // Hide the ActionBar
        supportActionBar?.hide()

        //initialize var
        contactUsTxt = findViewById(R.id.contactUs)
        bugtxt = findViewById(R.id.bug)

        //set click listener for contactUs text
        contactUsTxt.setOnClickListener {
            val intent = Intent(this, ContactUsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        //set the listener for the back button
        findViewById<ImageButton>(R.id.chevron_left).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        //set the click listener for the bug text
        bugtxt.setOnClickListener {
            val intent = Intent(this, ContactUsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        emergencyTxt = findViewById(R.id.emergency)
        emergencyTxt.setOnClickListener {
            showEmergencyDialog()
        }
    }

    private fun showEmergencyDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Emergency Call")
        builder.setMessage("Are you sure you want to call 911?")
        builder.setPositiveButton("Yes") { dialog, which ->
            callEmergencyNumber()
        }
        builder.setNegativeButton("No") { dialog, which ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun callEmergencyNumber() {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:911")
        startActivity(intent)
    }
}


