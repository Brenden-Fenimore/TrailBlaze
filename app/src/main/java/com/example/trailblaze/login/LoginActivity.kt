package com.example.trailblaze.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.trailblaze.MainActivity
import com.example.trailblaze.R

class LoginActivity : AppCompatActivity() {

    //declaration
    private lateinit var emailInput : EditText
    private lateinit var passwordInput : EditText
    private lateinit var loginBtn : Button
    private lateinit var forgotPasswordTextView : TextView

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


        //set click listener for register
        registertxt.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        //set the click listener for forgot password
        forgotPasswordTextView.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        //set click listener for terms
        termsAndCondtionstxt.setOnClickListener {
            //start terms and conditions activity
            val intent = Intent(this, TermsActivity::class.java)
            startActivity(intent)
        }

        //set click listener for login button
        loginBtn.setOnClickListener {
            val emailInput = emailInput.text.toString()
            val password = passwordInput.text.toString()

            //login logic here
            if (isValidEmail(emailInput) && password.isNotEmpty()) {

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
                // Show error message
                if (!isValidEmail(emailInput)) {
                    Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Please enter your password.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

private fun isValidEmail(email: String): Boolean {
    val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    return email.matches(emailPattern.toRegex())
}

