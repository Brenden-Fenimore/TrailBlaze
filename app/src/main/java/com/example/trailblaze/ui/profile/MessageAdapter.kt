package com.example.trailblaze.ui.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.ui.graphics.PathSegment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.R
import com.google.protobuf.Internal.ListAdapter
import com.example.trailblaze.ui.profile.MessageAdapter

class MessageAdapter : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>(){

    private val messages = mutableListOf<Message>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : MessageViewHolder{
        val view = LayoutInflater.from(parent.context)
        .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)

}
     override fun onBindViewHolder(holder: MessageViewHolder, position: Int){
       holder.bind(messages[position])
     }

    override fun getItemCount() : Int = messages.size

    fun submitList(newMessages: List<Message>){
        this.messages.clear()
        this.messages.addAll(newMessages)
        this.notifyDataSetChanged()
    }

inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)

    fun bind(message: Message) {
        messageTextView.text = message.toString()
    }
}
    class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message)= oldItem.timestamp == newItem.timestamp
        override fun areContentsTheSame(oldItem: Message, newItem: Message)= oldItem == newItem
    }
}