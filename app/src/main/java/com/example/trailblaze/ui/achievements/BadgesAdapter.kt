package com.example.trailblaze.ui.achievements

import android.content.ClipData
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.R

class BadgesAdapter(
    private var badges: List<Badge>,
    private val itemClickListener: ((Badge) -> Unit)? = null,
    private val sashDragListener: View.OnDragListener? = null
) : RecyclerView.Adapter<BadgesAdapter.ViewHolder>() {


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val badgeImage: ImageView = view.findViewById(R.id.badge_image)

        fun bind(badge: Badge) {
            badgeImage.setImageResource(badge.resourceId)

            // Set click listener for the badge
            itemView.setOnClickListener {
                itemClickListener?.invoke(badge)
            }

            // Start drag on touch
            badgeImage.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        val dragShadowBuilder = View.DragShadowBuilder(badgeImage)
                        v.startDragAndDrop(null, dragShadowBuilder, badgeImage, 0)
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


    // Function to update the badges list
    fun updateBadges(newBadges: List<Badge>) {
        this.badges = newBadges // Update the internal list of badges
        notifyDataSetChanged() // Notify the adapter that the data has changed
    }
}