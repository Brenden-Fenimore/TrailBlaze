package com.example.trailblaze.ui.achievements

import android.app.AlertDialog
import android.content.Context
import android.util.Log

class AchievementManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("user_achievements", Context.MODE_PRIVATE)

    // Save progress for achievements
    fun saveProgress(achievementId: String, progress: Int) {
        sharedPreferences.edit().putInt(achievementId, progress).apply()
    }

    // Get progress for achievements
    fun getProgress(achievementId: String): Int {
        return sharedPreferences.getInt(achievementId, 0)
    }

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

            // Update progress for Adventurer category
            updateProgressForAdventurerCategory()

            // Update the shared preferences
            unlockAchievement("safetyexpert")
            Log.d("AchievementManager", "Safety Expert badge granted.")
        } else {
            Log.d("AchievementManager", "Safety Expert badge already unlocked.")
        }
    }

    // Logic to grant a badge
    private fun grantBadge(badgeId: String) {
        Log.d("AchievementManager", "Granting badge: $badgeId")
    }

    private fun updateProgressForAdventurerCategory() {
        // Update progress logic
        val currentProgress = sharedPreferences.getInt("adventurer_progress", 0)

        val newProgress = currentProgress + 33

        // Cap the progress at 100%
        if (newProgress > 100) {
            sharedPreferences.edit().putInt("adventurer_progress", 100).apply()
        } else {
            sharedPreferences.edit().putInt("adventurer_progress", newProgress).apply()
        }
    }

    // Method to get the current progress for the Adventurer category
    fun getAdventurerProgress(): Int {
        return sharedPreferences.getInt("adventurer_progress", 0)
    }
}