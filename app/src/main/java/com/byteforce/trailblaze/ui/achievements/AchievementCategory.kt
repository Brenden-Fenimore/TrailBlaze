package com.byteforce.trailblaze.ui.achievements

// Data class representing a category of achievements in the application
data class AchievementCategory(
    val title: String,

    //list of resource IDs for the badges
    val badgeResourceIds: List<Int>,

    val tooltipTexts: List<String>,
)



