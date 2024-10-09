package com.example.trailblaze.Login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.trailblaze.MainActivity
import com.example.trailblaze.R
import com.example.trailblaze.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    //declaration
    lateinit var emailInput : EditText
    lateinit var passwordInput : EditText
    lateinit var loginBtn : Button
    lateinit var forgotPasswordTextView : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        //initialize views
        emailInput = findViewById(R.id.etEmail)
        passwordInput = findViewById(R.id.etPassword)
        loginBtn = findViewById(R.id.loginbtn)
        val registertxt = findViewById<TextView>(R.id.txtRegister)
        val termsAndCondtionstxt = findViewById<TextView>(R.id.termsandconditions)
        forgotPasswordTextView = findViewById(R.id.forgotPassword)


        //set click listener for login button
        loginBtn.setOnClickListener {
            val username = emailInput.text.toString()
            val password = passwordInput.text.toString()

            //login logic here
            if (username.isNotEmpty() && password.isNotEmpty()) {

                //successful login
                val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putBoolean("isLoggedIn", true)
                editor.apply()

                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()

                //navigate to MainActivity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()

            } else {
                Toast.makeText(this, "Please enter username and password.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
