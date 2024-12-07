package com.byteforce.trailblaze.ui.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.byteforce.trailblaze.R
import java.text.SimpleDateFormat
import java.util.*

class InboxAdapter(private val onConversationClick: (String, String, String?) -> Unit) :
    RecyclerView.Adapter<InboxAdapter.InboxViewHolder>() {

    private val conversations = mutableListOf<InboxConversation>()

    data class InboxConversation(
        val friendId: String,
        val friendName: String,
        val friendProfileImage: String?,
        val lastMessage: String,
        val timestamp: Long
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InboxViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_inbox_conversation, parent, false)
        return InboxViewHolder(view)
    }

    override fun onBindViewHolder(holder: InboxViewHolder, position: Int) {
        holder.bind(conversations[position])
    }

    override fun getItemCount() = conversations.size

    fun submitList(newConversations: List<InboxConversation>) {
        conversations.clear()
        conversations.addAll(newConversations)
        notifyDataSetChanged()
    }

    inner class InboxViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val friendNameView: TextView = itemView.findViewById(R.id.friendName)
        private val lastMessageView: TextView = itemView.findViewById(R.id.lastMessage)
        private val profileImageView: ImageView = itemView.findViewById(R.id.profileImage)
        private val timestampView: TextView = itemView.findViewById(R.id.timestamp)

        fun bind(conversation: InboxConversation) {
            friendNameView.text = conversation.friendName
            lastMessageView.text = conversation.lastMessage
            timestampView.text = formatTimestamp(conversation.timestamp)

            Glide.with(itemView)
                .load(conversation.friendProfileImage)
                .circleCrop()
                .into(profileImageView)

            itemView.setOnClickListener {
                onConversationClick(
                    conversation.friendId,
                    conversation.friendName,
                    conversation.friendProfileImage
                )
            }
        }
    }
    private fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}