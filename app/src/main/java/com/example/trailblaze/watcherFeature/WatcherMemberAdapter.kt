package com.example.trailblaze.watcherFeature
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.R
import com.example.trailblaze.watcherFeature.WatcherProfile
import android.content.Context
import android.content.Intent

class WatcherAdapter(
    private val context: Context, private val watcherList: List<WatcherMember>, private val onItemClick: (WatcherMember) -> Unit) :
    RecyclerView.Adapter<WatcherAdapter.WatcherViewHolder>() {

    class WatcherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val watcherName: TextView = itemView.findViewById(R.id.watcherName) // Change this line
        val watcherImage: ImageView = itemView.findViewById(R.id.watcherImage)
        val badgeImage: ImageView = itemView.findViewById(R.id.watcherBadgeImage)

        fun bind(context: Context, watcher: WatcherMember, onItemClick: (WatcherMember) -> Unit) {
            watcherName.text = watcher.watcherName
            watcherImage.setImageResource(watcher.watcherProfileImage)
            badgeImage.setImageResource(watcher.watcherBadgeImage)

            itemView.setOnClickListener {
                val intent = Intent(context, WatcherProfile::class.java).apply{
                    putExtra("watcherName", watcher.watcherName)
                    putExtra("watcherProfileImage", watcher.watcherProfileImage)
                    putExtra("watcherBadgeImage", watcher.watcherBadgeImage)
                }
                context.startActivity(intent)
                onItemClick(watcher)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WatcherViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_watcher_member, parent, false)
        return WatcherViewHolder(view)
    }

    override fun onBindViewHolder(holder: WatcherViewHolder, position: Int) {
        val watcher = watcherList[position]
        holder.bind(context, watcher, onItemClick)
    }

    override fun getItemCount(): Int {
        return watcherList.size
    }
}