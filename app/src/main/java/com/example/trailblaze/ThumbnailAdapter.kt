package com.example.trailblaze

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trailblaze.ui.parks.ParkDetailActivity

class ThumbnailAdapter(
    private var parkData: List<Pair<String, String>>,  // Pair of Image URL and Park Name
    private val onImageClick: (String) -> Unit
) : RecyclerView.Adapter<ThumbnailAdapter.ThumbnailViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThumbnailViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_thumbnail, parent, false)
        return ThumbnailViewHolder(view)
    }

    override fun onBindViewHolder(holder: ThumbnailViewHolder, position: Int) {
        val (imageUrl, parkName) = parkData[position]
        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(R.drawable.baseline_downloading_24) // Placeholder while loading
            .error(R.drawable.no_image_available) // Fallback image if loading fails
            .into(holder.imageView)

        // Set the park name
        holder.parkNameTextView.text = parkName

        // Set the click listener for the image
        holder.itemView.setOnClickListener {
            onImageClick(imageUrl)
        }
    }

    override fun getItemCount(): Int {
        return parkData.size
    }

    class ThumbnailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.thumbnailImageView)
        val parkNameTextView: TextView = itemView.findViewById(R.id.parkNameTextView)
    }

    fun updateData(newParkData: List<Pair<String, String>>) {
        parkData = newParkData
        notifyDataSetChanged()
    }
}