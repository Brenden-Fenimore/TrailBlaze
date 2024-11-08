// PendingRequestsAdapter.kt
package com.example.trailblaze.ui.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.R

// Data class representing a pending friend request with user ID and username
data class PendingRequest(val userId: String, val username: String)

class PendingRequestsAdapter(
    private val pendingRequests: List<PendingRequest>,          // List of pending requests to display
    private val onAcceptClicked: (String) -> Unit,              // Lambda for accept button action
    private val onDeclineClicked: (String) -> Unit              // Lambda for decline button action
) : RecyclerView.Adapter<PendingRequestsAdapter.PendingRequestViewHolder>() {

    // Inflates the layout for each item in the RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingRequestViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pending_request, parent, false)
        return PendingRequestViewHolder(view)
    }

    // Binds data to each item in the RecyclerView at a specific position
    override fun onBindViewHolder(holder: PendingRequestViewHolder, position: Int) {
        val request = pendingRequests[position]     // Get current pending request
        holder.usernameTextView.text = request.username     // Set the username text

        // Set click listeners for accept and decline actions
        holder.acceptButton.setOnClickListener { onAcceptClicked(request.userId) }
        holder.declineButton.setOnClickListener { onDeclineClicked(request.userId) }
    }

    // Returns the total count of items in the pendingRequests list
    override fun getItemCount(): Int = pendingRequests.size

    // ViewHolder class to hold references to each item's views
    class PendingRequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)   // TextView for username
        val acceptButton: Button = itemView.findViewById(R.id.acceptButton)       // Button to accept request
        val declineButton: Button = itemView.findViewById(R.id.declineButton)    // Button to decline request
    }
}
