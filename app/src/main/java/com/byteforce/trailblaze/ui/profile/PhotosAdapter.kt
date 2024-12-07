package com.byteforce.trailblaze.ui.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.byteforce.trailblaze.R

class PhotosAdapter(private var photoUrls: MutableList<String>, private val isOwnProfile: Boolean)  : RecyclerView.Adapter<PhotosAdapter.PhotoViewHolder>() {

    class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_photo, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val imageUrl = photoUrls[position]

        // Load image with Glide
        Glide.with(holder.imageView.context)
            .load(imageUrl)
            .override(120, 120)
            .centerCrop()
            .into(holder.imageView)

        // Set up click listener for full-screen view
        holder.itemView.setOnClickListener {
            val fragmentManager = (holder.itemView.context as AppCompatActivity).supportFragmentManager
            val fullscreenDialog = FullscreenImageDialogFragment.newInstance(photoUrls, position, isOwnProfile)

            // Set the adapter before showing the dialog
            fullscreenDialog.setPhotosAdapter(this) // Pass the current adapter

            fullscreenDialog.show(fragmentManager, "fullscreenDialog")
        }
    }


    override fun getItemCount(): Int {
        return photoUrls.size
    }

    // Function to update the list of photos
    fun updatePhotos(newPhotoUrls: List<String>) {
        photoUrls.clear() // CLear existing photos
        photoUrls.addAll(newPhotoUrls) // Add new photos
        notifyDataSetChanged() // Notify the adapter to refresh the RecyclerView
    }

    fun removeItem(position: Int) {
        photoUrls.removeAt(position)
        notifyItemRemoved(position)
    }


}