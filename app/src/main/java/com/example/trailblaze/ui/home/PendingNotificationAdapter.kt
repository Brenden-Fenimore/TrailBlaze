package com.example.trailblaze.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.R

class PendingNotificationAdapter : RecyclerView.Adapter<PendingNotificationAdapter.ViewHolder>() {

    private var notificationsList: List<String> = listOf() // Replace String with your notification model class if needed

    fun setNotifications(notifications: List<String>) {
        notificationsList = notifications
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false) // Create an XML layout for individual notifications
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = notificationsList[position]
        // Bind your notification data to the UI here
        holder.bind(notification)
    }

    override fun getItemCount(): Int {
        return notificationsList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(notification: String) {
            // Bind the notification data to the view here
            // e.g. itemView.notificationTextView.text = notification
        }
    }
}