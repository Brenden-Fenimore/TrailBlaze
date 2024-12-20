package com.byteforce.trailblaze.ui.achievements

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byteforce.trailblaze.R

// Fragment that displays a list of achievements
class AchievementsFragment : Fragment() {

    private lateinit var achievementManager: AchievementManager // Manager for handling achievement logic
    private lateinit var achievementsList: RecyclerView // RecyclerView to display achievements
    private lateinit var achievementsAdapter: AchievementsAdapter // Adapter for the RecyclerView
    private lateinit var categories: List<AchievementCategory> // List of achievement categories

    // Inflate the layout for the fragment and initialize components
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = inflater.inflate(R.layout.fragment_achievements, container, false)

        // Initialize the AchievementManager with the current context
        achievementManager = AchievementManager(requireContext())
        achievementsList = root.findViewById(R.id.achievementsRecyclerView)
        achievementsList.layoutManager = LinearLayoutManager(context)

        // Initialize categories
        categories = initializeAchievementCategories()
        achievementsAdapter = AchievementsAdapter(categories)
        achievementsList.adapter = achievementsAdapter

        return root
    }

    private fun initializeAchievementCategories(): List<AchievementCategory> {
        return listOf(
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
    }
}
