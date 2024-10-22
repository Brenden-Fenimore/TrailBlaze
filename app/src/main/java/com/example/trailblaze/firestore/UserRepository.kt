package com.example.trailblaze.firestore

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository(private val firestore: FirebaseFirestore) {

    fun getUserProfileImage(userId: String, onComplete: (String?) -> Unit) {
        val userRef = firestore.collection("users").document(userId)
        userRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val imageUrl = document.getString("profileImageUrl")
                    onComplete(imageUrl)
                } else {
                    onComplete(null) // No data found
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting user profile image: ${exception.message}")
                onComplete(null) // Handle error case
            }
    }
}