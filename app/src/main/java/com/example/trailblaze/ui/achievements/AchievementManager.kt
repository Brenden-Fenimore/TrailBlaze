package com.example.trailblaze.ui.achievements

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class AchievementManager(context: Context) {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Unlock an achievement
    fun unlockAchievement(achievementId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val userRef = firestore.collection("users").document(userId)

            // Update the achievement status in Firestore
            userRef.set(
                mapOf("${achievementId}_unlocked" to true),
                SetOptions.merge() // This will merge the new achievement status with existing data
            )
                .addOnSuccessListener {
                    Log.d("Firestore", "Achievement unlocked successfully: $achievementId")
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error unlocking achievement: ", e)
                }
        } else {
            Log.e("Firestore", "User not logged in")
        }
    }

    // Check if an achievement is unlocked
    fun isAchievementUnlocked(achievementId: String, onComplete: (Boolean) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val userRef = firestore.collection("users").document(userId)

            userRef.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val isUnlocked = document.getBoolean("${achievementId}_unlocked") ?: false
                        onComplete(isUnlocked)
                    } else {
                        onComplete(false)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error fetching achievement status: ", e)
                    onComplete(false)
                }
        } else {
            Log.e("Firestore", "User not logged in")
            onComplete(false)
        }
    }

    // Check and grant the Safety Expert badge
    fun checkAndGrantSafetyExpertBadge() {
        isAchievementUnlocked("safetyexpert") { hasSafetyExpertBadge ->
            if (!hasSafetyExpertBadge) {
                // Grant the badge
                grantBadge("safetyexpert")

                // Update Firestore
                unlockAchievement("safetyexpert")
            } else {
                Log.d("AchievementManager", "Safety Expert badge already unlocked.")
            }
        }
    }

    // Logic to grant a badge
    private fun grantBadge(badgeId: String) {
        Log.d("AchievementManager", "Granting badge: $badgeId")
        saveBadgeToUserProfile(badgeId)
    }

    fun saveBadgeToUserProfile(badgeId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val userRef = firestore.collection("users").document(userId)

            // Update the badges array in Firestore
            userRef.set(
                mapOf("badges" to FieldValue.arrayUnion(badgeId)),
                SetOptions.merge() // This will merge the new badge with existing data
            )
                .addOnSuccessListener {
                    // Badge successfully added
                    Log.d("Firestore", "Badge added successfully.")
                }
                .addOnFailureListener { e ->
                    // Handle any errors
                    Log.e("Firestore", "Error adding badge: ", e)
                }
        } else {
            Log.e("Firestore", "User not logged in")
        }
    }
}