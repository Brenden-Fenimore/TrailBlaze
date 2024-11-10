package com.example.trailblaze.ui.achievements

// Data class representing an achievement in the application
data class Achievement(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val criteria: Int= 0,
    val badge: Int = 0,
    var isUnlocked: Boolean = false
)