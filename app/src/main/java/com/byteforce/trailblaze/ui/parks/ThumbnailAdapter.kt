package com.byteforce.trailblaze.ui.parks

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.byteforce.trailblaze.R
import com.byteforce.trailblaze.nps.Park

class ThumbnailAdapter(
    private var parkData: List<Park>,
    private var parksList: List<Park>,
    private val onImageClick: (String) -> Unit // Change the type to String for parkCode
) : RecyclerView.Adapter<ThumbnailAdapter.ThumbnailViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThumbnailViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_thumbnail, parent, false)
        return ThumbnailViewHolder(view)
    }

    override fun onBindViewHolder(holder: ThumbnailViewHolder, position: Int) {
        val park = parkData[position]
        val imageUrl = park.images.firstOrNull()?.url ?: ""

        // Load the park image using Glide
        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(R.drawable.baseline_downloading_24)
            .error(R.drawable.no_image_available)
            .into(holder.imageView)

        // Set the park name
        holder.parkNameTextView.text = park.fullName

        holder.itemView.setOnClickListener {
            // Log the park code being clicked
            Log.d("ThumbnailAdapter", "Clicked park: ${park.fullName}, Park Code: ${park.parkCode}")

            // Trigger the click action using parkCode directly
            onImageClick(park.parkCode) // Pass parkCode instead of park or index
        }
    }

    override fun getItemCount(): Int {
        return parkData.size
    }

    class ThumbnailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.thumbnailImageView)
        val parkNameTextView: TextView = itemView.findViewById(R.id.parkNameTextView)
    }

    fun updateData(newParkData: List<Park>) {
        parkData = newParkData
        notifyDataSetChanged()
    }
}