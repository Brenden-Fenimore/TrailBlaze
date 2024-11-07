package com.example.trailblaze.ui.achievements

import android.content.ClipData
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.R

// Adapter for displaying a list of badges in a RecyclerView
class BadgesAdapter(
    private var badges: List<Badge>, // List of badges to be displayed
    private val itemClickListener: ((Badge) -> Unit) // Click listener for badge items
) : RecyclerView.Adapter<BadgesAdapter.ViewHolder>() {

    // Inner class to represent each badge item in the RecyclerView
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // ImageView to display the badge image
        val badgeImage: ImageView = view.findViewById(R.id.badge_image)

        init {
            // Set click listener for the badge item
            itemView.setOnClickListener {
                // Get the adapter position of the clicked item
                val position = adapterPosition
                // Check if the position is valid (not NO_POSITION)
                if (position != RecyclerView.NO_POSITION) {
                    // Get the badge at the clicked position
                    val badge = badges[position]
                    // Invoke the item click listener with the clicked badge
                    itemClickListener(badge)
                }
            }
        }

        fun bind(badge: Badge) {
            badgeImage.setImageResource(badge.resourceId)

            // Start drag on touch
            badgeImage.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // Create ClipData with the badge resource ID
                        val dragData = ClipData.newPlainText("badgeId", badge.resourceId.toString())
                        val dragShadowBuilder = View.DragShadowBuilder(badgeImage)

                        // Start the drag event
                        v.startDragAndDrop(dragData, dragShadowBuilder, badgeImage, 0)
                        true
                    }
                    else -> false
                }
            }
        }
    }

    // Override the onCreateViewHolder method to create a new ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate the badge_item layout and create a View for the ViewHolder
        val view = LayoutInflater.from(parent.context).inflate(R.layout.badge_item, parent, false)

        // Return a new instance of ViewHolder with the inflated view
        return ViewHolder(view)
    }

    // Override the onBindViewHolder method to bind data to the ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Get the badge at the current position in the list
        val badge = badges[position]

        // Bind the badge data to the ViewHolder
        holder.bind(badge)
    }

    // Override the getItemCount method to return the total number of items
    override fun getItemCount(): Int = badges.size

    // Function to update the badges list with a new list of badges
    fun updateBadges(newBadges: List<Badge>) {
        // Update the current badges list with the new list
        this.badges = newBadges

        // Notify the adapter that the dataset has changed and it should refresh the views
        notifyDataSetChanged()
    }
}