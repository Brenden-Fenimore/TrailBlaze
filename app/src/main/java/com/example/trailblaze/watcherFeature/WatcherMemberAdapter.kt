package com.example.trailblaze.watcherFeature

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.R
import com.example.trailblaze.watcherFeature.WatcherProfile.WatcherMember

class WatcherMemberAdapter(
    private val watcherMembers: List<WatcherMember>,
    private val onItemClick: (WatcherMember) -> Unit
) : RecyclerView.Adapter<WatcherMemberAdapter.WatcherMemberViewHolder>() {

    // ViewHolder to hold each item view
    inner class WatcherMemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImage: ImageView = itemView.findViewById(R.id.watcherImage)
        private val watcherName: TextView = itemView.findViewById(R.id.watcherMemberName)
        private val badgeImage: ImageView = itemView.findViewById(R.id.watcherBadgeImage)

        // Bind data to each view
        fun bind(watcher: WatcherMember) {
            watcherName.text = watcher.watcherName
            profileImage.setImageResource(watcher.watcherProfileImage)
            badgeImage.setImageResource(watcher.watcherBadgeImage)

            // Set onClickListener for each item
            itemView.setOnClickListener {
                onItemClick(watcher)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WatcherMemberViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_watcher_member, parent, false)
        return WatcherMemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: WatcherMemberViewHolder, position: Int) {
        holder.bind(watcherMembers[position])
    }

    override fun getItemCount(): Int = watcherMembers.size
}
