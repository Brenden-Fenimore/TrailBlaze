package com.example.trailblaze.ui.achievements

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

// Class responsible for managing achievements and badges for users
class AchievementManager(context: Context) {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance() // Firestore instance for database operations

    // Unlock an achievement
    fun unlockAchievement(achievementId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid // Get the current user's ID
        if (userId != null) {
            val userRef = firestore.collection("users").document(userId) // Reference to the user's document in Firestore

            // Update the achievement status in Firestore
            userRef.set(
                mapOf("${achievementId}_unlocked" to true), // Set the achievement as unlocked
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

    // Check if an achievement is unlocked for the current user
    fun isAchievementUnlocked(achievementId: String, onComplete: (Boolean) -> Unit) {
        // Get the current user ID from Firebase Authentication
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Check if the user ID is not null (i.e., user is logged in)
        if (userId != null) {
            // Reference to the user's document in the Firestore "users" collection
            val userRef = firestore.collection("users").document(userId)

            // Fetch the user's document from Firestore
            userRef.get()
                .addOnSuccessListener { document ->
                    // Check if the document exists
                    if (document != null) {
                        // Retrieve the unlocking status of the specified achievement
                        val isUnlocked = document.getBoolean("${achievementId}_unlocked") ?: false
                        // Call the onComplete callback with the unlocking status
                        onComplete(isUnlocked)
                    } else {
                        // If document does not exist, return false
                        onComplete(false)
                    }
                }
                .addOnFailureListener { e ->
                    // Log an error message if fetching the achievement status fails
                    Log.e("Firestore", "Error fetching achievement status: ", e)
                    // Call the onComplete callback with false to indicate failure
                    onComplete(false)
                }
        } else {
            // Log an error message if the user is not logged in
            Log.e("Firestore", "User not logged in")
            // Call the onComplete callback with false to indicate user is not logged in
            onComplete(false)
        }
    }

    // Function to check if the user qualifies for and grants the "Social Butterfly" badge
    fun checkAndGrantSocialButterflyBadge(userId: String) {
        // Reference to the user's document in Firestore
        val userRef = firestore.collection("users").document(userId)

        // Fetch the user's document from Firestore
        userRef.get().addOnSuccessListener { document ->
            // Check if the document exists
            if (document != null && document.exists()) {
                // Get the list of friends from the document, default to an empty list if null
                val friendsList = document.get("friends") as? List<String> ?: emptyList()

                // Check if the user has at least 1 friend (threshold can be adjusted)
                if (friendsList.size >= 1) { // Adjust the threshold if needed
                    // Check if the "Social Butterfly" achievement is unlocked
                    isAchievementUnlocked("socialbutterfly") { hasSocialButterflyBadge ->
                        // If the badge is not already unlocked
                        if (!hasSocialButterflyBadge) {
                            // Grant the badge and update Firestore
                            grantBadge("socialbutterfly") // Grant the badge
                            unlockAchievement("socialbutterfly") // Mark the achievement as unlocked
                        } else {
                            // Log that the badge is already unlocked
                            Log.d("AchievementManager", "Social Butterfly badge already unlocked.")
                        }
                    }
                }
            }
        }.addOnFailureListener { exception ->
            // Log an error message if fetching the user document fails
            Log.e("AchievementManager", "Error fetching user document: ", exception)
        }
    }


    // Check and grant the "Photographer" badge if it hasn't been unlocked yet
    fun checkAndGrantPhotographerBadge() {
        // Check if the "Photographer" achievement is unlocked
        isAchievementUnlocked("photographer") { hasPhotographerBadge ->
            // If the badge has not been unlocked
            if (!hasPhotographerBadge) {
                // Grant the "Photographer" badge
                grantBadge("photographer")

                // Update Firestore to mark the achievement as unlocked
                unlockAchievement("photographer")
            } else {
                // Log that the "Photographer" badge is already unlocked
                Log.d("AchievementManager", "Photographer badge already unlocked.")
            }
        }
    }

    // Check and grant the Safety Expert badge
    fun checkAndGrantSafetyExpertBadge() {
        isAchievementUnlocked("safetyexpert") { hasSafetyExpertBadge ->
            if (!hasSafetyExpertBadge) {
                // Grant the Safety Expert badge
                grantBadge("safetyexpert")
                // Update Firestore
                unlockAchievement("safetyexpert")

            } else {
                Log.d("AchievementManager", "Safety Expert badge already unlocked.")
            }
        }
    }

    // Check and grant the Badge Collector badge
    fun checkAndGrantBadgeCollectorBadge() {
        isAchievementUnlocked("badgecollector") { hasBadgeCollectorBadge ->
            if (!hasBadgeCollectorBadge) {
                // Grant the Badge Collector badge
                grantBadge("badgecollector")
                // Update Firestore
                unlockAchievement("badgecollector")
            } else {
                Log.d("AchievementManager", "Badge Collector badge already unlocked.")
            }
        }
    }

    // Check and grant the Badge Collector badge
    fun checkAndGrantCommunityBuilderBadge() {
        isAchievementUnlocked("communitybuilder") { hasBadgeCollectorBadge ->
            if (!hasBadgeCollectorBadge) {
                // Grant the Badge Collector badge
                grantBadge("communitybuilder")
                // Update Firestore
                unlockAchievement("communitybuilder")
            } else {
                Log.d("AchievementManager", "Badge Collector badge already unlocked.")
            }
        }
    }

    // Check and grant the Badge Collector badge
    fun checkAndGrantConquerorBadge() {
        isAchievementUnlocked("conqueror") { hasBadgeCollectorBadge ->
            if (!hasBadgeCollectorBadge) {
                // Grant the Badge Collector badge
                grantBadge("conqueror")
                // Update Firestore
                unlockAchievement("conqueror")
            } else {
                Log.d("AchievementManager", "Conqueror Badge already unlocked.")
            }
        }
    }

    // Check and grant the Leaderboard badge
    fun checkAndGrantLeaderboardBadge() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val userRef = firestore.collection("users").document(userId)

            userRef.get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Get the list of badges
                    val badgesList = document.get("badges") as? List<String> ?: emptyList()

                    // Check if the user has at least 2 badges
                    if (badgesList.size >= 2) {
                        isAchievementUnlocked("leaderboard") { hasLeaderboardBadge ->
                            if (!hasLeaderboardBadge) {
                                // Grant the badge and update Firestore
                                grantBadge("leaderboard")
                                unlockAchievement("leaderboard")
                            } else {
                                Log.d("AchievementManager", "Leaderboard badge already unlocked.")
                            }
                        }
                    }
                }
            }.addOnFailureListener { exception ->
                Log.e("AchievementManager", "Error fetching user document: ", exception)
            }
        } else {
            Log.e("Firestore", "User not logged in")
        }
    }

    // Logic to grant a badge to the user
    private fun grantBadge(badgeId: String) {
        // Log the action of granting the badge with its ID
        Log.d("AchievementManager", "Granting badge: $badgeId")
        // Save the badge to the user's profile
        saveBadgeToUserProfile(badgeId)
    }

    // Function to save the granted badge to the user's profile in Firestore
    fun saveBadgeToUserProfile(badgeId: String) {
        // Get the current user's ID from Firebase Authentication
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Check if the user ID is not null (i.e., user is logged in)
        if (userId != null) {
            // Reference to the user's document in Firestore
            val userRef = firestore.collection("users").document(userId)

            // Update the badges array in Firestore by adding the new badge
            userRef.set(
                mapOf("badges" to FieldValue.arrayUnion(badgeId)), // Use arrayUnion to add the badge without duplicating
                SetOptions.merge() // This will merge the new badge with existing data
            )
                .addOnSuccessListener {
                    // Log a success message if the badge is added successfully
                    Log.d("Firestore", "Badge added successfully.")
                }
                .addOnFailureListener { e ->
                    // Log an error message if there is an issue adding the badge
                    Log.e("Firestore", "Error adding badge: ", e)
                }
        } else {
            // Log an error message if the user is not logged in
            Log.e("Firestore", "User not logged in")
        }
    }
}