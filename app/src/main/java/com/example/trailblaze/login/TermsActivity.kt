package com.example.trailblaze.login

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.trailblaze.R

class TermsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms)

        //find ok button id
        val okButton = findViewById<Button>(R.id.termsOKbtn)

        //set click listener for ok button and close page
        okButton.setOnClickListener{
            finish()
        }
    }
}
