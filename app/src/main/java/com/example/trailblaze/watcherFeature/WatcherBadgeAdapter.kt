package com.example.trailblaze.watcherFeature

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trailblaze.R
import com.example.trailblaze.ui.achievements.Badge



class WatcherBadgeAdapter(
    private val watcherBadges: List<WatcherBadgeDetails>, // List of badges to be displayed
     private val itemClickListener: (WatcherBadgeDetails) // Click listener for badge items
) : RecyclerView.Adapter<WatcherBadgeAdapter.WatcherBadgeViewHolder>() {

    // Listener for badge interactions

    interface BadgeTouchListener {
        fun onDescriptionShown(badge: WatcherBadgeDetails, view: View)
        fun onDescriptionHidden(badge: WatcherBadgeDetails, view: View)
    }

    var badgeTouchListener: BadgeTouchListener? = null

    // Override the onCreateViewHolder method to create a new ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WatcherBadgeAdapter.WatcherBadgeViewHolder {
        // Inflate the watcher_badge_item layout and create a View for the ViewHolder
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_watcher_badge, parent, false)

        // Return a new instance of ViewHolder with the inflated view
        return WatcherBadgeViewHolder(view)
    }

    // Override the onBindViewHolder method to bind data to the ViewHolder
    override fun onBindViewHolder(holder: WatcherBadgeViewHolder, position: Int) {
        // Get the badge at the current position in the list
        val badge = watcherBadges[position]

        // Bind the badge data to the ViewHolder
        holder.bind(badge)
    }

    // Override the getItemCount method to return the total number of items
    override fun getItemCount(): Int = watcherBadges.size

    // Inner class to represent each badge item in the RecyclerView
    inner class WatcherBadgeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // ImageView to display the badge image and title
       private val badgeIcon: ImageView = itemView.findViewById(R.id.badgeIcon)
        private val badgeTitle: TextView = itemView.findViewById(R.id.badgeTitle)

        fun bind(badge: WatcherBadgeDetails) {
           badgeTitle.text = badge.title
            Glide.with(itemView.context).load(badge.icon).into(badgeIcon)


            badgeIcon.setOnTouchListener { view, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        badgeTouchListener?.onDescriptionShown(badge, view)
                        view.performClick()
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        badgeTouchListener?.onDescriptionHidden(badge, view)
                    }
                }
                true
            }
                }
            }


    }
