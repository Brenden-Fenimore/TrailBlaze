package com.example.trailblaze.firestore

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.trailblaze.R

object ImageLoader {

    fun loadProfilePicture(context: Context, imageView: ImageView, imageUrl: String?) {
        if (imageUrl != null) {
            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.account_circle) // Placeholder image while loading
                .error(R.drawable.account_circle) // Error image if the load fails
                .into(imageView)
        } else {
            imageView.setImageResource(R.drawable.account_circle) // Set a default image
        }
    }
}
