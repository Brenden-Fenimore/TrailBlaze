package com.example.trailblaze.ui.achievements

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.R

class AchievementsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var achievementsAdapter: AchievementsAdapter
    private lateinit var yourBadges: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievements)
        // Hide the ActionBar
        supportActionBar?.hide()

        findViewById<ImageButton>(R.id.chevron_left).setOnClickListener{
            onBackPressedDispatcher.onBackPressed()
        }

        // Initialize your button
        yourBadges = findViewById(R.id.your_badges)

        // Set the onClickListener for the button
        yourBadges.setOnClickListener {
            // Create an Intent to start BadgesActivity
            val intent = Intent(this, BadgesActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
        }

        //list of achievement categories
        val categories = listOf(
            AchievementCategory(
                title = "Distance",
                badgeResourceIds = listOf(R.drawable.trailblaze_logo, R.drawable.mountainclimber, R.drawable.trekker),
                tooltipTexts = listOf(
                    getString(R.string.trailblazer),
                    getString(R.string.mountainclimber),
                    getString(R.string.longdistancetrekker)
                ),
            ),
            AchievementCategory(
                title = "Frequency",
                badgeResourceIds = listOf(R.drawable.hhiker, R.drawable.weekendwarrior, R.drawable.dailyadventurer),
                tooltipTexts = listOf(
                    getString(R.string.habitualhiker),
                    getString(R.string.weekendwarrior),
                    getString(R.string.dailyadventurer)
                ),
            ),
            AchievementCategory(
                title = "Difficulty",
                badgeResourceIds = listOf(R.drawable.conqueror, R.drawable.explorer, R.drawable.trailmaster),
                tooltipTexts = listOf(
                    getString(R.string.conqueror),
                    getString(R.string.explorer),
                    getString(R.string.trailmaster),
                ),
            ),
            AchievementCategory(
                title = "Group Hiking",
                badgeResourceIds = listOf(R.drawable.socialbutterfly, R.drawable.teamplayer, R.drawable.communitybuilder),
                tooltipTexts = listOf(
                    getString(R.string.socialbutterfly),
                    getString(R.string.teamplayer),
                    getString(R.string.communitybuilder),
                ),
            ),
            AchievementCategory(
                title = "Adventurer",
                badgeResourceIds = listOf(R.drawable.wildlifewatcher, R.drawable.photographer, R.drawable.safetyexpert),
                tooltipTexts = listOf(
                    getString(R.string.wildlifewatcher),
                    getString(R.string.photographer),
                    getString(R.string.safteyexpert)
                ),
            ),
            AchievementCategory(
                title = "Goal Achiever",
                badgeResourceIds = listOf(R.drawable.goalsetter, R.drawable.badgecollector, R.drawable.leaderboard),
                tooltipTexts = listOf(
                    getString(R.string.goalsetter),
                    getString(R.string.badgecollector),
                    getString(R.string.leaderboardtip)
                ),
            )
        )

        //set up the RecyclerView
        recyclerView = findViewById(R.id.recycler_view_achievements)
        recyclerView.layoutManager = LinearLayoutManager(this)
        achievementsAdapter = AchievementsAdapter(categories)
        recyclerView.adapter = achievementsAdapter
    }
}