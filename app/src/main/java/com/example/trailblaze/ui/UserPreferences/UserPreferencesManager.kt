package com.example.trailblaze.ui.UserPreferences

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserPreferencesManager {

    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    // Method to fetch user preferences
    fun fetchUserPreferences(onSuccess: (UserPreferences) -> Unit, onFailure: (String) -> Unit) {
        val userId = auth.currentUser?.uid

        if (userId != null) {
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Create a UserPreferences object from the retrieved data
                        val userPreferences = UserPreferences(
                            city = document.getString("city"),
                            state = document.getString("state"),
                            zip = document.getString("zip"),
                            distance = document.getDouble("distance")
                        )
                        onSuccess(userPreferences)
                    } else {
                        onFailure("No preferences found for this user.")
                    }
                }
                .addOnFailureListener { e ->
                    onFailure(e.message ?: "Error fetching preferences")
                }
        } else {
            onFailure("User not logged in. Cannot fetch preferences.")
        }
    }
}
