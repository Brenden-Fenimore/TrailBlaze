package com.byteforce.trailblaze.ui.parks

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.byteforce.trailblaze.R

// Adapter for displaying a list of friends in a RecyclerView for adding friends
class AddFriendsAdapter(
    private val friendList: List<AddFriend>, // List of friends to be displayed
    private val onCheckChange: (AddFriend) -> Unit // Callback function to handle checkbox state changes
) : RecyclerView.Adapter<AddFriendsAdapter.FriendViewHolder>() {

    // ViewHolder class to hold the views for each friend item
    class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // TextView to display the friend's username
        val userNameTextView: TextView = itemView.findViewById(R.id.friend_username_text_view)
        // CheckBox to select or deselect the friend
        val checkBox: CheckBox = itemView.findViewById(R.id.friend_checkbox)
    }

    // Creates a new ViewHolder for the friend item layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        // Inflate the layout for each friend item
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_add_friend, parent, false)
        // Return a new FriendViewHolder with the inflated view
        return FriendViewHolder(itemView)
    }

    // Binds data to the ViewHolder at the specified position
    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        // Get the current friend from the list based on the position
        val currentFriend = friendList[position]

        // Set the username text in the TextView
        holder.userNameTextView.text = currentFriend.username

        // Set the checkbox state based on the friend's selection status
        holder.checkBox.isChecked = currentFriend.isSelected

        // Handle checkbox change
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            // Update the selected status of the current friend based on the checkbox state
            currentFriend.isSelected = isChecked

            // Notify the listener of the change in checkbox state
            onCheckChange(currentFriend)
        }
    }

    override fun getItemCount(): Int {
        return friendList.size
    }
}