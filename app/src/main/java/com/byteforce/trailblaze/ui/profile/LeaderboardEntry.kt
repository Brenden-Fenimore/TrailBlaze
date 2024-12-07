package com.byteforce.trailblaze.ui.profile

data class LeaderboardEntry(
    val userId: String,
    val username: String,
    val badgeCount: Int,
    val profileImageUrl: String?
)