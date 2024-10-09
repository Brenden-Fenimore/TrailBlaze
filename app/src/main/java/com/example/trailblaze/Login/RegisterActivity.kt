package com.example.trailblaze.Login

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.trailblaze.R
import android.content.Intent
import android.widget.*
import androidx.core.widget.addTextChangedListener
import com.example.trailblaze.MainActivity

class RegisterActivity : AppCompatActivity() {

    //declaration
    private lateinit var checkBoxTerms :CheckBox
    private lateinit var createAccountButton: Button
    private lateinit var etUsername : EditText
    private lateinit var etPassword : EditText
    private lateinit var etConfirmPassword : EditText
    private lateinit var etDateofBirth : EditText
    private lateinit var etEmail : EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        //initialize views
        val termsAndCondtionstxt = findViewById<TextView>(R.id.termsandconditions)
        checkBoxTerms = findViewById(R.id.checkBoxTerms)
        createAccountButton = findViewById(R.id.createAccountBtn)
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etpw1)
        etConfirmPassword = findViewById(R.id.etConfirmpw)
        etDateofBirth = findViewById(R.id.etDateofbirth)
        etEmail = findViewById(R.id.etEmail)
        val logintxt = findViewById<TextView>(R.id.txtLogin)

        //set clicker listener for logintxt
        logintxt.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        //set up listeners for the checkbox and input fields
        checkBoxTerms.setOnCheckedChangeListener { _, _ ->
            updateCreateAccountButtonState()
        }

        //add text change listeners to update the button
        etUsername.addTextChangedListener { updateCreateAccountButtonState() }
        etDateofBirth.addTextChangedListener { updateCreateAccountButtonState() }
        etEmail.addTextChangedListener { updateCreateAccountButtonState() }
        etPassword.addTextChangedListener { updateCreateAccountButtonState() }
        etConfirmPassword.addTextChangedListener { updateCreateAccountButtonState()
        }

        //set click listener for terms
        termsAndCondtionstxt.setOnClickListener {
            //start terms and conditions activity
            val intent = Intent(this, TermsAndConditins::class.java)
            startActivity(intent)
        }

        //set click listener for create account button
        createAccountButton.setOnClickListener {
            //check if checkbox is not clicked
            if(!checkBoxTerms.isChecked) {
                Toast.makeText(this, "You must agree to the terms to create an account", Toast.LENGTH_SHORT).show()
            }
            else if (validateInputs()){
                createAccount()
            }
        }
    }

    //navigate to personalize account and reset stack
    private fun createAccount() {

        val username = etUsername.text.toString()
        val password = etPassword.text.toString()
        val dateOfBirth = etDateofBirth.text.toString()
        val email = etEmail.text.toString()

        //save loggedIn status to true
        val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isLoggedIn", true)
        editor.apply()

        Toast.makeText(this,"Account created successfully!",Toast.LENGTH_SHORT).show()

        //navigate to personalize account
        val intent = Intent(this, PersonalizeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    //function to update create account button to change its state
    private fun updateCreateAccountButtonState() {
        createAccountButton.isEnabled =
            checkBoxTerms.isChecked && validateInputs()
    }

    //function to make sure all Ui elements are filled out
    private fun validateInputs():Boolean{
        val username = etUsername.text.toString()
        val password = etPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()
        val dateOfBirth = etDateofBirth.text.toString()
        val email = etEmail.text.toString()

        //check for empty fields
        return when {
            username.isEmpty() -> {
                Toast.makeText(this, "Username is required", Toast.LENGTH_LONG).show()
                false
            }
            dateOfBirth.isEmpty() -> {
                Toast.makeText(this, "Date of birth is required", Toast.LENGTH_LONG).show()
                false
            }
            email.isEmpty() -> {
                Toast.makeText(this, "Email is required", Toast.LENGTH_LONG).show()
                false
            }
            password.isEmpty() -> {
                Toast.makeText(this, "Password is required", Toast.LENGTH_LONG).show()
                false
            }
            confirmPassword.isEmpty()->{
                Toast.makeText(this, "Confirm password is required", Toast.LENGTH_LONG).show()
                false
            }
            else-> true
        }
    }
}

