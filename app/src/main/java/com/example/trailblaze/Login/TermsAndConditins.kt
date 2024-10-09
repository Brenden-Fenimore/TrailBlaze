package com.example.trailblaze.Login

import android.os.Bundle
import android.widget.Button
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.trailblaze.R
import com.example.trailblaze.databinding.ActivityTermsAndConditinsBinding

class TermsAndConditins : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms_and_conditins)

        val okButton = findViewById<Button>(R.id.termsOKbtn)

        //set click listener for ok button and close page
        okButton.setOnClickListener{
            finish()
        }
    }
}