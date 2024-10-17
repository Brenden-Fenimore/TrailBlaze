package com.example.trailblaze.ui.achievements

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.R

class AchievementsFragment : Fragment() {

    private lateinit var achievementManager: AchievementManager
    private lateinit var achievementsList: RecyclerView
    private lateinit var achievementsAdapter: AchievementsAdapter
    private lateinit var categories: List<AchievementCategory>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = inflater.inflate(R.layout.fragment_achievements, container, false)

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
                progress = 0
            ),
            AchievementCategory(
                title = "Frequency",
                badgeResourceIds = listOf(R.drawable.hhiker, R.drawable.weekendwarrior, R.drawable.dailyadventurer),
                tooltipTexts = listOf(
                    getString(R.string.habitualhiker),
                    getString(R.string.weekendwarrior),
                    getString(R.string.dailyadventurer)
                ),
                progress = 0
            ),
            AchievementCategory(
                title = "Difficulty",
                badgeResourceIds = listOf(R.drawable.conqueror, R.drawable.explorer, R.drawable.trailmaster),
                tooltipTexts = listOf(
                    getString(R.string.conqueror),
                    getString(R.string.explorer),
                    getString(R.string.trailmaster),
                ),
                progress = 0
            ),
            AchievementCategory(
                title = "Group Hiking",
                badgeResourceIds = listOf(R.drawable.socialbutterfly, R.drawable.teamplayer, R.drawable.communitybuilder),
                tooltipTexts = listOf(
                    getString(R.string.socialbutterfly),
                    getString(R.string.teamplayer),
                    getString(R.string.communitybuilder),
                ),
                progress = 0
            ),
            AchievementCategory(
                title = "Adventurer",
                badgeResourceIds = listOf(R.drawable.wildlifewatcher, R.drawable.photographer, R.drawable.safetyexpert),
                tooltipTexts = listOf(
                    getString(R.string.wildlifewatcher),
                    getString(R.string.photographer),
                    getString(R.string.safteyexpert)
                ),
                progress = 0
            ),
            AchievementCategory(
                title = "Goal Achiever",
                badgeResourceIds = listOf(R.drawable.goalsetter, R.drawable.badgecollector, R.drawable.leaderboard),
                tooltipTexts = listOf(
                    getString(R.string.goalsetter),
                    getString(R.string.badgecollector),
                    getString(R.string.leaderboardtip)
                ),
                progress = 0
            )
        )
    }

    fun onSafetyTipsNavigated() {
        achievementManager.checkAndGrantSafetyExpertBadge()

        // Update the categories list after granting the badge
        refreshProgress()
    }

    private fun refreshProgress() {
        // Update the progress for the Adventurer category
        val adventurerProgress = achievementManager.getAdventurerProgress()

        // Update the relevant category in the list
        categories.find { it.title == "Adventurer" }?.progress = adventurerProgress

        // Notify the adapter to refresh the display
        achievementsAdapter.notifyDataSetChanged()
    }
}
