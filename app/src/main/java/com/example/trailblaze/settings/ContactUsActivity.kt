package com.example.trailblaze.settings

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.trailblaze.R

class ContactUsActivity : AppCompatActivity() {

    //declaration
    private lateinit var cancelBtn: Button
    private lateinit var submitBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_us)

        //find var by id
        cancelBtn=findViewById(R.id.cancel)
        submitBtn=findViewById(R.id.submit)

        //set the listener for the back button
        findViewById<ImageButton>(R.id.chevron_left).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        //set the click listener to close the screen
        cancelBtn.setOnClickListener {
            finish()
        }

        //set the click listener to close the screen
        submitBtn.setOnClickListener {
            finish()
        }

        }
    }

