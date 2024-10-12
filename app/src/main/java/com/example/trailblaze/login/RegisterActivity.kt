package com.example.trailblaze.login

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.trailblaze.R
import android.content.Intent
import android.widget.*
import androidx.core.widget.addTextChangedListener
import com.example.trailblaze.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    //declaration
    private lateinit var checkBoxTerms :CheckBox
    private lateinit var createAccountButton: Button
    private lateinit var etUsername : EditText
    private lateinit var etPassword : EditText
    private lateinit var etConfirmPassword : EditText
    private lateinit var etDateofBirth : EditText
    private lateinit var etEmail : EditText

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        //initialize Firestore and FirebaseAuth
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

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
            val intent = Intent(this, TermsActivity::class.java)
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
                saveLoggedInstate()
                navigateToPersonalizeActivity()
            }
        }
    }

    //create account function using firebase
    private fun createAccount() {
        val username = etUsername.text.toString()
        val password = etPassword.text.toString()
        val dateOfBirth = etDateofBirth.text.toString()
        val email = etEmail.text.toString()

        //create user with Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = hashMapOf(
                        "username" to username,
                        "dateOfBirth" to dateOfBirth,
                        "email" to email
                    )

                    //save user information to Firestore
                    val userId = auth.currentUser?.uid // Get the user's unique ID
                    firestore.collection("users").document(userId!!)
                        .set(user)
                        .addOnCompleteListener { firestoreTask ->
                            if (firestoreTask.isSuccessful) {
                                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "Failed to create account!", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(this, "Registration failed!", Toast.LENGTH_SHORT).show()
                }
            }
    }

    //function to navigate to PersonalizeActivity
    private fun navigateToPersonalizeActivity() {
        //navigate to personalize account
        val intent = Intent(this, PersonalizeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    //function to save loggedIn state
    private fun saveLoggedInstate(){
        //save loggedIn status to true
        val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isLoggedIn", true)
        editor.apply()

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
            !isValidDate(dateOfBirth) -> {
                Toast.makeText(this, "Date of birth must be in the format xx/xx/xxxx", Toast.LENGTH_LONG).show()
                false
            }
            email.isEmpty() -> {
                Toast.makeText(this, "Email is required", Toast.LENGTH_LONG).show()
                false
            }
            !isValidEmail(email) -> {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_LONG).show()
                false
            }
            password.isEmpty() -> {
                Toast.makeText(this, "Password is required", Toast.LENGTH_LONG).show()
                false
            }
            password.length < 6 -> {
                Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_LONG).show()
                false
            }
            confirmPassword.isEmpty()->{
                Toast.makeText(this, "Confirm password is required", Toast.LENGTH_LONG).show()
                false
            }
            password != confirmPassword -> {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_LONG).show()
                false
            }
            else-> true
        }
    }
}

//validate email format
private fun isValidEmail(email: String): Boolean {
    // Regular expression for validating an email
    val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    return email.matches(emailPattern.toRegex())
}

//validate date format (xx/xx/xxxx)
private fun isValidDate(date: String): Boolean {
    // Regular expression for validating date format xx/xx/xxxx
    val datePattern = "\\d{2}/\\d{2}/\\d{4}"
    return date.matches(datePattern.toRegex())
}

