package com.example.trailblaze.ui.profile

data class LeaderboardEntry(
    val userId: String,
    val username: String,
    val badgeCount: Int,
    val profileImageUrl: String?
)