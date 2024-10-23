package com.example.trailblaze.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.example.trailblaze.firestore.User
object UserManager {
    private var currentUser: User? = null

    fun getCurrentUser(): User? {
        return currentUser
    }

    fun setCurrentUser(user: User) {
        currentUser = user
    }

    fun fetchUserData(userId: String, firestore: FirebaseFirestore, callback: (User?) -> Unit) {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val user = User(
                        uid = document.id,
                        username = document.getString("username") ?: "",
                        state = document.getString("state") ?: "",
                        city = document.getString("city") ?: "",
                        dateOfBirth = document.getString("dateOfBirth") ?: "",
                        difficulty = document.getString("difficulty") ?: "",
                        fitnessLevel = document.getString("fitnessLevel") ?: "",
                        phone = document.getString("phone") ?: "",
                        zip = document.getString("zip") ?: "",
                        profileImageUrl = document.getString("profileImageUrl") ?: "",
                        distance = document.getDouble("distance") ?: 0.0,
                        email = document.getString("email") ?: "",
                        friends = document.get("friends") as? List<String> ?: emptyList()
                    )
                    setCurrentUser(user)
                    callback(user)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener { exception ->
                callback(null)
            }
    }
}