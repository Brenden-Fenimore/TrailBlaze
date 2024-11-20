package com.example.trailblaze.firestore

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

// Class responsible for managing user-related data operations in Firestore
class UserRepository(private val firestore: FirebaseFirestore) {

    // Function to get the user's profile image URL
    fun getUserProfileImage(userId: String, onComplete: (String?) -> Unit) {

        // Reference to the user's document in the Firestore "users" collection
        val userRef = firestore.collection("users").document(userId)
        userRef.get()
            // Retrieve the document data
            .addOnSuccessListener { document ->
                if (document != null) {
                    // Get the profile image URL from the document
                    val imageUrl = document.getString("profileImageUrl")
                    // Invoke the callback with the imageUrl
                    onComplete(imageUrl)
                } else {
                    // If no document was found, invoke the callback with null
                    onComplete(null) // No data found
                }
            }
            .addOnFailureListener { exception ->
                // Log the error message if the fetch operation fails
                Log.e("Firestore", "Error getting user profile image: ${exception.message}")
                // Invoke the callback with null to indicate failure
                onComplete(null) // Handle error case
            }
    }
    // Function to update the user's profile with new data
    fun updateUserProfile(userId: String, updatedUserData: Map<String, Any>, onComplete: (Boolean) -> Unit) {
        // Reference to the user's document in the Firestore "users" collection
        val userRef = firestore.collection("users").document(userId)
        // Update the document with the new user data
        userRef.update(updatedUserData)
            .addOnSuccessListener {
                // Log success message if the update is successful
                Log.d("Firestore", "User profile updated successfully")
                // Invoke the callback with true to indicate success
                onComplete(true)
            }
            .addOnFailureListener { exception ->
                // Log the error message if the update operation fails
                Log.e("Firestore", "Error updating user profile: ${exception.message}")
                // Invoke the callback with false to indicate failure
                onComplete(false)
            }
    }
}