// PendingRequestsAdapter.kt
package com.example.trailblaze.ui.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.R

data class PendingRequest(val userId: String, val username: String)

class PendingRequestsAdapter(
    private val pendingRequests: List<PendingRequest>,
    private val onAcceptClicked: (String) -> Unit,
    private val onDeclineClicked: (String) -> Unit
) : RecyclerView.Adapter<PendingRequestsAdapter.PendingRequestViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingRequestViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pending_request, parent, false)
        return PendingRequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: PendingRequestViewHolder, position: Int) {
        val request = pendingRequests[position]
        holder.usernameTextView.text = request.username
        holder.acceptButton.setOnClickListener { onAcceptClicked(request.userId) }
        holder.declineButton.setOnClickListener { onDeclineClicked(request.userId) }
    }

    override fun getItemCount(): Int = pendingRequests.size

    class PendingRequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
        val acceptButton: Button = itemView.findViewById(R.id.acceptButton)
        val declineButton: Button = itemView.findViewById(R.id.declineButton)
    }
}
