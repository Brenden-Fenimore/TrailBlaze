package com.example.trailblaze.settings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.trailblaze.R
import com.example.trailblaze.login.TermsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PrivacyAndSecurityActivity : AppCompatActivity() {
    private lateinit var dprivateSwitch: Switch
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_and_security)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        dprivateSwitch = findViewById(R.id.dprivateSwitch)

        // Load current privacy setting from Firestore
        loadPrivacySetting()

        // Set the listener for the switch
        dprivateSwitch.setOnCheckedChangeListener { _, isChecked ->
            savePrivacySetting(isChecked)
        }

        // Set the listener for the back button
        findViewById<ImageButton>(R.id.chevron_left).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<TextView>(R.id.termsAndCon).setOnClickListener {
            startActivity(Intent(this, TermsActivity::class.java))
        }
    }

    private fun loadPrivacySetting() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val isPrivateAccount = document.getBoolean("isPrivateAccount") ?: false
                    dprivateSwitch.isChecked = isPrivateAccount
                }
            }
            .addOnFailureListener { exception ->
                Log.e("PrivacyAndSecurityActivity", "Error fetching privacy setting: ", exception)
            }
    }

    private fun savePrivacySetting(isPrivate: Boolean) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = firestore.collection("users").document(userId)

        userRef.update("isPrivateAccount", isPrivate)
            .addOnSuccessListener {
                Log.d("PrivacyAndSecurityActivity", "Privacy setting updated successfully.")
            }
            .addOnFailureListener { exception ->
                Log.e("PrivacyAndSecurityActivity", "Error updating privacy setting: ", exception)
            }
    }
}