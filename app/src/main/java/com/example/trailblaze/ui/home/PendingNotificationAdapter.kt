package com.example.trailblaze.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.R

class PendingNotificationAdapter(
    private val onNotificationChecked: (String, Boolean) -> Unit
) : RecyclerView.Adapter<PendingNotificationAdapter.ViewHolder>() {

    private val notificationsList: MutableList<Pair<String, Boolean>> = mutableListOf() // Pair<Notification, IsRead>

    fun setNotifications(pendingNotifications: List<String>, reviewedNotifications: List<String>) {
        notificationsList.clear() // Clear any existing notifications

        // Add pending notifications as unread (false)
        notificationsList.addAll(pendingNotifications.map { Pair(it, false) })

        // Add reviewed notifications as read (true)
        notificationsList.addAll(reviewedNotifications.map { Pair(it, true) })

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notificationPair = notificationsList[position]
        val notification = notificationPair.first
        val isRead = notificationPair.second

        holder.bind(notification, isRead)

        // Disable checkbox listener for reviewed notifications
        holder.checkBox.setOnCheckedChangeListener(null)

        holder.checkBox.isChecked = isRead

        // Only allow checking for pending notifications
        holder.checkBox.isEnabled = !isRead

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (!isRead) { // Only react to changes for pending notifications
                onNotificationChecked(notification, isChecked)
            }
        }
    }

    override fun getItemCount(): Int {
        return notificationsList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val notificationTextView: TextView = itemView.findViewById(R.id.notificationTextView)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)

        fun bind(notification: String, isRead: Boolean) {
            notificationTextView.text = notification
            checkBox.isChecked = isRead
        }
    }
}