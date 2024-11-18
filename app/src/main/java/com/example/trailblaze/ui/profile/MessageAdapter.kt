package com.example.trailblaze.ui.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.R

class MessageAdapter (private val onMessageClick: (Message) -> Unit) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    private val messages = mutableListOf<Message>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : MessageViewHolder{
        val view = LayoutInflater.from(parent.context)
        .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)

}
     override fun onBindViewHolder(holder: MessageViewHolder, position: Int){
       holder.bind(messages[position])
         holder.itemView.setOnClickListener { onMessageClick(messages[position]) }
     }

    override fun getItemCount() : Int = messages.size

    fun submitList(newMessages: List<Message>){
        this.messages.clear()
        this.messages.addAll(newMessages)
        this.notifyDataSetChanged()
    }

inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val senderImageView: ImageView = itemView.findViewById(R.id.senderImageView)
    private val senderNameTextView: TextView = itemView.findViewById(R.id.senderNameTextView)
    private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
    private val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)

    fun bind(message: Message) {
       // senderNameTextView.text = message.senderName
        messageTextView.text = message.toString()
        timestampTextView.text = formatTimestamp(message.timestamp)
    }

    private fun formatTimestamp(timestamp: Long): String {
        // Convert the timestamp to a human-readable date
        val sdf = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }

}
    class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message)= oldItem.timestamp == newItem.timestamp
        override fun areContentsTheSame(oldItem: Message, newItem: Message)= oldItem == newItem
    }
}