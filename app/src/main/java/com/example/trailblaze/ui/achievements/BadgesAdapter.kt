package com.example.trailblaze.ui.achievements

import android.content.ClipData
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.R

class BadgesAdapter(
    private var badges: List<Badge>,
    private val itemClickListener: ((Badge) -> Unit)
) : RecyclerView.Adapter<BadgesAdapter.ViewHolder>() {

    // Property to hold the current badges
    var currentBadges: List<Badge> = badges

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val badgeImage: ImageView = view.findViewById(R.id.badge_image)

        init {
            // Set click listener for the badge item
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    // Get the badge at the clicked position
                    val badge = badges[position]
                    // Call the item click listener
                    itemClickListener(badge)
                }
            }
        }


        fun bind(badge: Badge) {
            badgeImage.setImageResource(badge.resourceId)

            // Set click listener for the badge
            itemView.setOnClickListener {
                itemClickListener.invoke(badge)
            }

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.badge_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val badge = badges[position]
        holder.bind(badge)
    }

    override fun getItemCount(): Int = badges.size


    // Update the badges list
    fun updateBadges(newBadges: List<Badge>) {
        this.badges = newBadges
        this.currentBadges = newBadges // Update current badges to the new list
        notifyDataSetChanged() // Notify the adapter that the data has changed
    }


}