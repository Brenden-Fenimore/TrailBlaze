package com.example.trailblaze.ui.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trailblaze.R

class LeaderboardAdapter(private var entries: List<LeaderboardEntry>) : RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImageView: ImageView = itemView.findViewById(R.id.profileImageView)
        val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
        val badgeCountTextView: TextView = itemView.findViewById(R.id.badgeCountTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.leaderboard_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = entries[position]
        holder.usernameTextView.text = entry.username
        holder.badgeCountTextView.text = entry.badgeCount.toString()

        // Load the user's profile picture using Glide
        Glide.with(holder.itemView.context)
            .load(entry.profileImageUrl) // Load the profile image URL
            .placeholder(R.drawable.baseline_downloading_24) // Placeholder while loading
            .error(R.drawable.profile_no_image_available) // Fallback image if loading fails
            .circleCrop() // To display the image in a circular format
            .into(holder.profileImageView)
    }

    override fun getItemCount(): Int {
        return entries.size
    }

    // Method to update the list of entries
    fun updateEntries(newEntries: List<LeaderboardEntry>) {
        entries = newEntries
        notifyDataSetChanged()
    }
}