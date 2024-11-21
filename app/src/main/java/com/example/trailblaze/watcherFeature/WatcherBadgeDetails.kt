package com.example.trailblaze.watcherFeature

data class WatcherBadgeDetails(
    val title: String,
    val description: String,
)

object WatcherBadgeRepository{
    private val badgeDetails = listOf(
        WatcherBadgeDetails("Most Valuable Watcher", "Awarded to watchers who are consistently chosen as a watcher for a specific user."),
        WatcherBadgeDetails("Master Watcher", "An iconic presence in the TrailBlaze community, ensuring more than 150 safe adventures."),
        WatcherBadgeDetails("Night Owl Watcher", "Awarded to watchers who have watched 10 night adventures."),
        WatcherBadgeDetails("Novice Watcher", "Awarded to watchers after completing their 1st watch."),
        WatcherBadgeDetails("Tenth Watch", "Awarded to watchers after completing their 10th watch."),
        WatcherBadgeDetails("Twenty-Fifth Watch", "Awarded to watchers after completing their 25th watch."),
        WatcherBadgeDetails("Fiftieth Watch", "Awarded to watchers after completing their 50th watch."),
        WatcherBadgeDetails("Seventy- Fifth Watch", "Awarded to watchers after completing their 75th watch."),
        WatcherBadgeDetails("Hundredth Watch", "Awarded to watchers after completing their 100th watch."),
        WatcherBadgeDetails("Pathfinder Protector", "Awarded to watchers who’ve supported 30 adventures, always keeping the path clear for exploration."),
        WatcherBadgeDetails("Trail Steward", "Awarded to watchers with a vigilant eye, guiding adventurers safely across 60 journeys."),
        WatcherBadgeDetails("Guardian of the Trail", "Awarded to watchers who are true watcher champions, safeguarding 100 treks."),
        WatcherBadgeDetails("Highlander Guardian", "Master of the peaks, providing steadfast safety in lofty terrains, these watchers have supported TrailBlazers through at least 5 trails classified as mountainous."),
        WatcherBadgeDetails("Milestone Maven", "Awarded to watchers with cumulative trail milestones."),
        WatcherBadgeDetails("First Responder", "Awarded to watchers who respond to a notification within 5 minutes at least 10 times, these watchers are lightning-fast and dependable, a watcher you can trust when it matters most."),
        WatcherBadgeDetails("After Adventure Ally", "You’re a true companion, always checking in to celebrate successes and ensure safety. This badge is awarded to watchers with a Follow-Up Rate of 76% or higher.")
    )

    fun getBadgeDescription(title: String) : String{
        return badgeDetails.find { it.title == title }?.description ?: "Description not available"

    }
}