package com.byteforce.trailblaze.ui.parks

data class TimeRecord(
    val parkName: String,
    val elapsedTime: String,
    val imageUrl: String?,
    val parkCode: String,
    val timestamp: Long,
    val date: String,
    val place: Boolean = false,
    val placeId: String? = null,
)