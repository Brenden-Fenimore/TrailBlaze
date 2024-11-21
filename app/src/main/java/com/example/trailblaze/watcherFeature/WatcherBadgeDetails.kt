package com.example.trailblaze.watcherFeature

import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.example.trailblaze.R

data class WatcherBadgeDetails(
    val id: String = " ",
    val title: String = " ",
    val description: String  = " ",
    val icon: Int = R.drawable.default_badge
)

object WatcherBadgeRepository{
    private val badgeDetails = listOf(
        WatcherBadgeDetails("001","Most Valuable Watcher", "Awarded to watchers who are consistently chosen as a watcher for a specific user.", R.drawable.most_valuable_watcher),
        WatcherBadgeDetails("002","Master Watcher", "An iconic presence in the TrailBlaze community, ensuring more than 150 safe adventures.", R.drawable.master_watcher),
        WatcherBadgeDetails("003","Night Owl Watcher", "Awarded to watchers who have watched 10 night adventures.", R.drawable.night_owl_watcher),
        WatcherBadgeDetails("004","Novice Watcher", "Awarded to watchers after completing their 1st watch.", R.drawable.novice_watcher_badge),
//        WatcherBadgeDetails("005","Tenth Watch", "Awarded to watchers after completing their 10th watch."),
//        WatcherBadgeDetails("006","Twenty-Fifth Watch", "Awarded to watchers after completing their 25th watch."),
//        WatcherBadgeDetails("007","Fiftieth Watch", "Awarded to watchers after completing their 50th watch."),
//        WatcherBadgeDetails("008","Seventy- Fifth Watch", "Awarded to watchers after completing their 75th watch."),
//        WatcherBadgeDetails("009","Hundredth Watch", "Awarded to watchers after completing their 100th watch."),
//        WatcherBadgeDetails("010","Pathfinder Protector", "Awarded to watchers who’ve supported 30 adventures, always keeping the path clear for exploration."),
//        WatcherBadgeDetails("011","Trail Steward", "Awarded to watchers with a vigilant eye, guiding adventurers safely across 60 journeys."),
//        WatcherBadgeDetails("012","Guardian of the Trail", "Awarded to watchers who are true watcher champions, safeguarding 100 treks."),
//        WatcherBadgeDetails("013","Highlander Guardian", "Master of the peaks, providing steadfast safety in lofty terrains, these watchers have supported TrailBlazers through at least 5 trails classified as mountainous."),
//        WatcherBadgeDetails("014","Milestone Maven", "Awarded to watchers with cumulative trail milestones."),
//        WatcherBadgeDetails("015","First Responder", "Awarded to watchers who respond to a notification within 5 minutes at least 10 times, these watchers are lightning-fast and dependable, a watcher you can trust when it matters most."),
//        WatcherBadgeDetails("016","After Adventure Ally", "You’re a true companion, always checking in to celebrate successes and ensure safety. This badge is awarded to watchers with a Follow-Up Rate of 76% or higher.")
    )

    fun getBadgeDescription(title: String) : String{
        return badgeDetails.find { it.title == title }?.description ?: "Description not available"

    }
}