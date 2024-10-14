package com.example.trailblaze.ui.achievements

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.R

class AchievementsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var achievementsAdapter: AchievementsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievements)

        //list of achievement categories
        val categories = listOf(
            AchievementCategory(
                title = "Distance",
                badgeResourceIds = listOf(R.drawable.trailblaze_logo, R.drawable.mountainclimber, R.drawable.trekker),
                progress = 75 // Example progress
            ),
            AchievementCategory(
                title = "Frequency",
                badgeResourceIds = listOf(R.drawable.hhiker, R.drawable.weekendwarrior, R.drawable.dailyadventurer),
                progress = 0
            ),
            AchievementCategory(
                title = "Difficulty",
                badgeResourceIds = listOf(R.drawable.conqueror, R.drawable.explorer, R.drawable.trailmaster),
                progress = 0
            ),
            AchievementCategory(
                title = "Group Hiking",
                badgeResourceIds = listOf(R.drawable.socialbutterfly, R.drawable.teamplayer, R.drawable.communitybuilder),
                progress = 0
            ),
            AchievementCategory(
                title = "Adventurer",
                badgeResourceIds = listOf(R.drawable.wildlifewatcher, R.drawable.photographer, R.drawable.safetyexpert),
                progress = 0
            ),
            AchievementCategory(
                title = "Goal Achiever",
                badgeResourceIds = listOf(R.drawable.goalsetter, R.drawable.badgecollector, R.drawable.leaderboard),
                progress = 40
            )
        )

        //set up the RecyclerView
        recyclerView = findViewById(R.id.recycler_view_achievements)
        recyclerView.layoutManager = LinearLayoutManager(this)
        achievementsAdapter = AchievementsAdapter(categories)
        recyclerView.adapter = achievementsAdapter
    }
}