package com.example.trailblaze.watcherFeature
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.R
import com.example.trailblaze.watcherFeature.WatcherProfile

class WatcherMemberAdapter(private val watcherList: List<WatcherMember>) :
    RecyclerView.Adapter<WatcherMemberAdapter.WatcherViewHolder>() {

    // Define the ViewHolder class
    class WatcherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val watcherName: TextView = itemView.findViewById(R.id.watcherName)
        val watcherImage: ImageView = itemView.findViewById(R.id.watcherImage)
        val badgeImage: ImageView = itemView.findViewById(R.id.watcherBadgeImage) // New badge ImageView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WatcherViewHolder {
        // Inflate the layout for each item in the RecyclerView
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_watcher_member, parent, false)
        return WatcherViewHolder(view)
    }

    override fun onBindViewHolder(holder: WatcherViewHolder, position: Int) {
        // Get the current watcher from the list
        val watcher = watcherList[position]

        // Bind the data to the views
        holder.watcherName.text = watcher.watcherName
        holder.watcherImage.setImageResource(watcher.watcherProfileImage)
        //holder.badgeImage.setImageResource(watcher.badgeImage)
    }

    override fun getItemCount(): Int = watcherList.size // Return the size of the list
}
