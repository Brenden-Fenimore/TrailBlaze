package com.example.trailblaze.ui.achievements

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class AchievementManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("user_achievements", Context.MODE_PRIVATE)
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    // Unlock an achievement
    fun unlockAchievement(achievementId: String) {
        sharedPreferences.edit().putBoolean("${achievementId}_unlocked", true).apply()
    }

    // Check if an achievement is unlocked
    fun isAchievementUnlocked(achievementId: String): Boolean {
        return sharedPreferences.getBoolean("${achievementId}_unlocked", false)
    }

    // Check and grant the Safety Expert badge
    fun checkAndGrantSafetyExpertBadge() {
        val hasSafetyExpertBadge = isAchievementUnlocked("safetyexpert")

        if (!hasSafetyExpertBadge) {
            // Grant the badge
            grantBadge("safetyexpert")

            // Update the shared preferences
            unlockAchievement("safetyexpert")
        } else {
            Log.d("AchievementManager", "Safety Expert badge already unlocked.")
        }
    }

    // Logic to grant a badge
    private fun grantBadge(badgeId: String) {
        Log.d("AchievementManager", "Granting badge: $badgeId")
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