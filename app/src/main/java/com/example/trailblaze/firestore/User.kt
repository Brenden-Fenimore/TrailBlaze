package com.example.trailblaze.firestore

import com.example.trailblaze.nps.Park
// Data class representing a user
data class User(
    val uid: String = "",
    val username: String = "",
    val state: String = "",
    val city: String = "",
    val dateOfBirth: String = "",
    val difficulty: String = "",
    val distance: Double? = 0.0,
    val email: String = "",
    val fitnessLevel: String = "",
    val phone: String = "",
    val zip: String = "",
    val profileImageUrl: String? = "",
    val friends: List<String> = emptyList(),
    val badges: List<String> = emptyList(),
    val favoriteParks: List<Park> = emptyList(),
    val bucketListParks: List<Park> = emptyList(),
    val isPrivateAccount: Boolean,
    val favoriteTrailsVisible: Boolean,
    val leaderboardVisible: Boolean,
    val photosVisible: Boolean,
    val shareLocationVisible: Boolean,
    val watcherVisible: Boolean,
    val photos: List<String> = emptyList()

)