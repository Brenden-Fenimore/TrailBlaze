package com.example.trailblaze.firestore

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.trailblaze.R

object ImageLoader {

    // Function to load a profile picture into an ImageView
    fun loadProfilePicture(context: Context, imageView: ImageView, imageUrl: String?) {
        // Check if the imageUrl is not null
        if (imageUrl != null) {
            // Use Glide to load the image from the URL
            Glide.with(context) // Initialize Glide with the context
                .load(imageUrl) // Specify the URL to load the image from
                .placeholder(R.drawable.account_circle) // Placeholder image while loading
                .error(R.drawable.account_circle) // Error image if the load fails
                .into(imageView) // Load the image into the specified ImageView
        } else {
            imageView.setImageResource(R.drawable.account_circle) // Set a default image
        }
    }
}
