package com.example.trailblaze.ui.parks

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.R

class AddFriendsAdapter(
    private val friendList: List<AddFriend>,
    private val onCheckChange: (AddFriend) -> Unit
) : RecyclerView.Adapter<AddFriendsAdapter.FriendViewHolder>() {

    class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userNameTextView: TextView = itemView.findViewById(R.id.friend_username_text_view)
        val checkBox: CheckBox = itemView.findViewById(R.id.friend_checkbox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_add_friend, parent, false)
        return FriendViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val currentFriend = friendList[position]
        holder.userNameTextView.text = currentFriend.username
        holder.checkBox.isChecked = currentFriend.isSelected

        // Handle checkbox change
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            currentFriend.isSelected = isChecked
            onCheckChange(currentFriend) // Notify the listener of the change
        }
    }

    override fun getItemCount(): Int {
        return friendList.size
    }
}