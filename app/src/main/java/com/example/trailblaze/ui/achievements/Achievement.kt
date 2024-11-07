package com.example.trailblaze.ui.achievements

// Data class representing an achievement in the application
data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val criteria: Int,
    val badge: Int,
    var isUnlocked: Boolean = false
)