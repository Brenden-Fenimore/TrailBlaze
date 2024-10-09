package com.example.trailblaze.Login

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.trailblaze.R
import com.example.trailblaze.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : AppCompatActivity() {

    //declaration
    private lateinit var etEmail: EditText
    private lateinit var resetPasswordBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        //initialize
        etEmail = findViewById(R.id.etEmail)
        resetPasswordBtn = findViewById(R.id.resetPassword)

        //reset password button listener
        resetPasswordBtn.setOnClickListener {
            val email = etEmail.text.toString()
            if(email.isEmpty()){
                Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show()
            }
            else{
                //let them know that an emaill was sent to reset password
                Toast.makeText(this, "Password reset email sent to $email", Toast.LENGTH_SHORT).show()

                //return to the main scree
                finish()
            }
        }
        //set the listener for the back button
        findViewById<ImageButton>(R.id.chevron_left).setOnClickListener {
            onBackPressed()
        }

    }
}