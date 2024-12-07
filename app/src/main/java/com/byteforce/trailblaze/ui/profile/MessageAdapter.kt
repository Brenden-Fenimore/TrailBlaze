package com.byteforce.trailblaze.ui.profile

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.byteforce.trailblaze.R
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(
    private val currentUserProfileUrl: String?,
    private val friendProfileUrl: String?,
    private val onMessageClick: (Message) -> Unit
) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    private val messages = mutableListOf<Message>()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }

    override fun getItemCount(): Int = messages.size

    fun submitList(newMessages: List<Message>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageContainer: LinearLayout = itemView.findViewById(R.id.messageContainer)
        private val messageContentContainer: LinearLayout = itemView.findViewById(R.id.messageContentContainer)
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        private val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)
        private val leftProfileImage: ImageView = itemView.findViewById(R.id.leftProfileImage)
        private val rightProfileImage: ImageView = itemView.findViewById(R.id.rightProfileImage)

        fun bind(message: Message) {
            messageTextView.text = message.text
            timestampTextView.text = formatTimestamp(message.timestamp)

            val params = messageContainer.layoutParams as FrameLayout.LayoutParams
            if (message.senderId == currentUserId) {
                params.gravity = Gravity.END
                messageContentContainer.setBackgroundResource(R.drawable.sent_message_background)
                rightProfileImage.visibility = View.VISIBLE
                leftProfileImage.visibility = View.GONE
                Glide.with(itemView)
                    .load(currentUserProfileUrl)
                    .circleCrop()
                    .into(rightProfileImage)
            } else {
                params.gravity = Gravity.START
                messageContentContainer.setBackgroundResource(R.drawable.received_message_background)
                leftProfileImage.visibility = View.VISIBLE
                rightProfileImage.visibility = View.GONE
                Glide.with(itemView)
                    .load(friendProfileUrl)
                    .circleCrop()
                    .into(leftProfileImage)
            }
            messageContainer.layoutParams = params
        }
    }

    private fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}