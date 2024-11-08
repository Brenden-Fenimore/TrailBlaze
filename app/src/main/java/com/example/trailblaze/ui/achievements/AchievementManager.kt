package com.example.trailblaze.ui.achievements

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

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

    // Function to check and grant the Conqueror badge after completing 3 trails
    fun checkAndGrantConquerorBadge() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Fetch the user's completed trails from Firestore
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Assuming 'completedTrails' is a field that stores the number of completed trails
                    val completedTrails = document.getLong("completedTrails") ?: 0

                    if (completedTrails >= 3) {
                        // Check if the Conqueror badge is already unlocked
                        isAchievementUnlocked("conqueror") { hasBadgeCollectorBadge ->
                            if (!hasBadgeCollectorBadge) {
                                // Grant the Conqueror badge
                                grantBadge("conqueror")
                                // Update Firestore
                                unlockAchievement("conqueror")
                                Log.d("AchievementManager", "Conqueror Badge granted.")
                            } else {
                                Log.d("AchievementManager", "Conqueror Badge already unlocked.")
                            }
                        }
                    } else {
                        Log.d("AchievementManager", "User has completed $completedTrails trails. Badge not granted.")
                    }
                } else {
                    Log.d("AchievementManager", "User document does not exist.")
                }
            }
            .addOnFailureListener { e ->
                Log.e("AchievementManager", "Error fetching user data: ${e.message}")
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

    // Check and grant the Badge Collector badge
    fun checkAndGrantTrailBlazerBadge() {
        isAchievementUnlocked("trailblazer") { hasBadgeCollectorBadge ->
            if (!hasBadgeCollectorBadge) {
                // Grant the Badge Collector badge
                grantBadge("trailblazer")
                // Update Firestore
                unlockAchievement("trailblazer")
            } else {
                Log.d("AchievementManager", "TrailBlazer Badge already unlocked.")
            }
        }
    }

    // Check and grant the Badge Collector badge
    fun checkAndGrantMountainClimberBadge() {
        isAchievementUnlocked("mountainclimber") { hasBadgeCollectorBadge ->
            if (!hasBadgeCollectorBadge) {
                // Grant the Badge Collector badge
                grantBadge("mountainclimber")
                // Update Firestore
                unlockAchievement("mountainclimber")
            } else {
                Log.d("AchievementManager", "Mountain Climber Badge already unlocked.")
            }
        }
    }

    // Check and grant the Badge Collector badge
    fun checkAndGrantLongDistanceBadge() {
        isAchievementUnlocked("longdistancetrekker") { hasBadgeCollectorBadge ->
            if (!hasBadgeCollectorBadge) {
                // Grant the Badge Collector badge
                grantBadge("longdistancetrekker")
                // Update Firestore
                unlockAchievement("longdistancetrekker")
            } else {
                Log.d("AchievementManager", "Long Distance Badge already unlocked.")
            }
        }
    }

    fun checkAndGrantHabitualBadge() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val oneWeekAgo = System.currentTimeMillis() - (7 *24* 60 *60* 1000) // Current time minus 7 days

        // Fetch the user's trails from Firestore
        firestore.collection("users").document(userId).collection("trails")
            .whereGreaterThan("completionTime", oneWeekAgo) // Assuming 'completionTime' is a timestamp
            .get()
            .addOnSuccessListener { querySnapshot ->
                val completedTrailsCount = querySnapshot.size() // Get the count of completed trails in the last week

                if (completedTrailsCount >= 2) {
                    // Check if the Habitual Hiker badge is already unlocked
                    isAchievementUnlocked("habitualhiker") { hasBadgeCollectorBadge ->
                        if (!hasBadgeCollectorBadge) {
                            // Grant the Habitual Hiker badge
                            grantBadge("habitualhiker")
                            // Update Firestore
                            unlockAchievement("habitualhiker")
                            Log.d("AchievementManager", "Habitual Hiker Badge granted.")
                        } else {
                            Log.d("AchievementManager", "Habitual Hiker Badge already unlocked.")
                        }
                    }
                } else {
                    Log.d("AchievementManager", "User completed $completedTrailsCount trails in the last week. Badge not granted.")
                }
            }
            .addOnFailureListener { e ->
                Log.e("AchievementManager", "Error fetching user trails: ${e.message}")
            }
    }

    // Check and grant the Weekend Warrior badge
    fun checkAndGrantWeekendBadge(timestamp: Long) {
        // Create a Calendar instance and set the time
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp

        // Get the day of the week
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        // Check if the day is Saturday (7) or Sunday (1)
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            isAchievementUnlocked("weekendwarrior") { hasBadgeCollectorBadge ->
                if (!hasBadgeCollectorBadge) {
                    // Grant the Weekend Warrior badge
                    grantBadge("weekendwarrior")
                    // Update Firestore
                    unlockAchievement("weekendwarrior")
                    Log.d("AchievementManager", "Weekend Warrior Badge granted.")
                } else {
                    Log.d("AchievementManager", "Weekend Warrior Badge already unlocked.")
                }
            }
        } else {
            Log.d("AchievementManager", "Trail completed on a weekday; no badge granted.")
        }
    }

    fun checkForDailyAdventurerBadge(userId: String) {
        val hikeDatesRef = firestore.collection("users").document(userId).collection("hikeDates")

        // Fetch the hike dates
        hikeDatesRef.get()
            .addOnSuccessListener { querySnapshot ->
                // Create a list to hold the unique dates
                val hikeDates = mutableListOf<String>()

                // Loop through the documents to extract dates
                for (document in querySnapshot.documents) {
                    val date = document.getDate("date")?.let { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it) }
                    if (date != null && !hikeDates.contains(date)) {
                        hikeDates.add(date)
                    }
                }

                // Check for 7 consecutive days
                if (hasSevenConsecutiveDays(hikeDates)) {
                    grantDailyAdventurerBadge(userId)
                }
            }
            .addOnFailureListener { e ->
                Log.e("AchievementManager", "Error retrieving hike dates: ${e.message}")
            }
    }

    // Method to grant the Daily Adventurer badge
    private fun grantDailyAdventurerBadge(userId: String) {
        // Calculate the timestamps for the last 7 days
        val calendar = Calendar.getInstance()
        val today = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, -6) // Go back 6 days to include today
        val oneWeekAgo = calendar.timeInMillis

        // Fetch the user's trails from Firestore for the last week
        firestore.collection("users").document(userId).collection("trails")
            .whereGreaterThan("completionTime", oneWeekAgo)
            .get()
            .addOnSuccessListener { querySnapshot ->
                // To track the unique days the user completed trails
                val completedDays = mutableSetOf<String>()

                querySnapshot.forEach { document ->
                    val completionTime = document.getLong("completionTime") ?: return@forEach
                    val date = Date(completionTime)
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    completedDays.add(dateFormat.format(date)) // Add the date in "yyyy-MM-dd" format to the set
                }

                // Check if the user completed trails for all 7 days
                if (completedDays.size >= 7) {
                    // Check if the Daily Adventurer badge is already unlocked
                    isAchievementUnlocked("dailyadventurer") { hasBadge ->
                        if (!hasBadge) {
                            // Grant the badge and unlock the achievement
                            grantBadge("dailyadventurer")
                            unlockAchievement("dailyadventurer")
                            Log.d("AchievementManager", "Daily Adventurer badge granted.")
                        } else {
                            Log.d("AchievementManager", "Daily Adventurer badge already unlocked.")
                        }
                    }
                } else {
                    Log.d("AchievementManager", "User completed trails on ${completedDays.size} unique days. Badge not granted.")
                }
            }
            .addOnFailureListener { e ->
                Log.e("AchievementManager", "Error fetching user trails: ${e.message}")
            }
    }

    private fun hasSevenConsecutiveDays(dates: List<String>): Boolean {
        // Convert date strings to Date objects
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateObjects = dates.mapNotNull { date ->
            try {
                dateFormat.parse(date)
            } catch (e: ParseException) {
                null // Ignore invalid date formats
            }
        }.sorted() // Sort the dates in ascending order

        // Check for consecutive days
        var consecutiveCount = 1 // Start counting from the first date

        for (i in 1 until dateObjects.size) {
            val currentDate = dateObjects[i]
            val previousDate = dateObjects[i - 1]

            // Check if the current date is exactly one day after the previous date
            val calendar = Calendar.getInstance()
            calendar.time = previousDate
            calendar.add(Calendar.DAY_OF_YEAR, 1)

            if (calendar.time == currentDate) {
                consecutiveCount++
            } else {
                consecutiveCount = 1 // Reset the count if not consecutive
            }

            // If we have found 7 consecutive days, return true
            if (consecutiveCount >= 7) {
                return true
            }
        }
        // If we finish the loop without finding 7 consecutive days, return false
        return false
    }

    fun checkAndGrantExplorerBadge(completionTime: Long) {
        // Create a Calendar instance to check the hour
        val calendar = Calendar.getInstance().apply {
            timeInMillis = completionTime
        }

        // Get the hour of the day (in 24-hour format)
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)

        // Define evening hours (for example, 17:00 - 23:59)
        if (hourOfDay >= 17) { // Change this to your desired evening start hour
            isAchievementUnlocked("explorer") { hasExplorerBadge ->
                if (!hasExplorerBadge) {
                    // Grant the Explorer badge
                    grantBadge("explorer")
                    // Update Firestore
                    unlockAchievement("explorer")
                    Log.d("AchievementManager", "Explorer Badge granted for evening trail completion.")
                } else {
                    Log.d("AchievementManager", "Explorer Badge already unlocked.")
                }
            }
        } else {
            Log.d("AchievementManager", "Trail completed outside of evening hours, no badge granted.")
        }
    }

    fun checkAndGrantTeamPlayerBadge(isPartyHike: Boolean) {
        if (isPartyHike) { // Only check for the badge if it's a party hike
            isAchievementUnlocked("teamplayer") { hasTeamPlayerBadge ->
                if (!hasTeamPlayerBadge) {
                    // Grant the Team Player badge
                    grantBadge("teamplayer")
                    // Update Firestore
                    unlockAchievement("teamplayer")
                    Log.d("AchievementManager", "Team Player Badge granted.")
                } else {
                    Log.d("AchievementManager", "Team Player Badge already unlocked.")
                }
            }
        } else {
            Log.d("AchievementManager", "Not a party hike, Team Player Badge not granted.")
        }
    }

    // Check and grant the Badge Collector badge
    fun checkAndGrantWildlifeBadge() {
        isAchievementUnlocked("wildlifewatcher") { hasBadgeCollectorBadge ->
            if (!hasBadgeCollectorBadge) {
                // Grant the Badge Collector badge
                grantBadge("wildlifewatcher")
                // Update Firestore
                unlockAchievement("wildlifewatcher")
            } else {
                Log.d("AchievementManager", "Wildlife Watcher Badge already unlocked.")
            }
        }
    }

    // Check and grant the Badge Collector badge
    fun checkAndGrantGaolBadge() {
        isAchievementUnlocked("goalsetter") { hasBadgeCollectorBadge ->
            if (!hasBadgeCollectorBadge) {
                // Grant the Badge Collector badge
                grantBadge("goalsetter")
                // Update Firestore
                unlockAchievement("goalsetter")
            } else {
                Log.d("AchievementManager", "Goal Setter Badge already unlocked.")
            }
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