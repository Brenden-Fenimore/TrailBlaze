package com.example.trailblaze.settings

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.trailblaze.R


class SupportScreenActivity : AppCompatActivity() {

    //declaration
    private lateinit var contactUsTxt: TextView
    private lateinit var bugtxt : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_support_screen)

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
        }
    }

