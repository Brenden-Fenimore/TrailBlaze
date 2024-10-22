package com.example.trailblaze.ui.achievements

data class AchievementCategory(
    val title: String,

    //list of resource IDs for the badges
    val badgeResourceIds: List<Int>,

    val tooltipTexts: List<String>,

    //progress percentage
    val progress: Int
)