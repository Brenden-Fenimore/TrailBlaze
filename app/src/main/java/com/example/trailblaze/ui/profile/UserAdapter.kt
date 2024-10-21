package com.example.trailblaze.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.databinding.ItemUserBinding
import com.example.trailblaze.firestore.ImageLoader

class UserAdapter(
    private var users: List<User>, // Replace User with your user data model class
    private val onClick: (User) -> Unit,
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(private val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.usernameTextView.text = user.username // Assuming you have a TextView for the username
            // Load the profile picture using an image loading library
            ImageLoader.loadProfilePicture(binding.root.context, binding.profileImageView, user.profileImageUrl)

            binding.root.setOnClickListener {
                onClick(user) // Invoke the click listener when the item is clicked
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false) // Inflate your item layout
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size

    fun updateUserList(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }
}