package com.example.trailblaze.firestore
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
)