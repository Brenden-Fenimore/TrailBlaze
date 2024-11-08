package com.example.trailblaze.ui.parks

data class TimeRecord(
    val parkName: String,
    val elapsedTime: String,
    val imageUrl: String?, // Include image URL
    val parkCode: String,
    val timestamp: Long,
    val date: String
)