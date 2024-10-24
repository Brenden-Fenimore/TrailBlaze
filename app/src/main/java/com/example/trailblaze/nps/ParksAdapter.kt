package com.example.trailblaze.nps

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trailblaze.R

// Adapter class for binding park data to RecyclerView items
class ParksAdapter(var parksList: List<Park>, private val onParkClick: (Park) -> Unit) : RecyclerView.Adapter<ParksAdapter.ParkViewHolder>() {

    // ViewHolder for holding views for each item
    class ParkViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val thumbnailImageView: ImageView = view.findViewById(R.id.thumbnailImageView)
        val parkNameTextView: TextView = view.findViewById(R.id.parkNameTextView)
    }

    // Inflate the layout for each RecyclerView item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParkViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_park, parent, false)
        return ParkViewHolder(view)
    }

    override fun onBindViewHolder(holder: ParkViewHolder, position: Int) {
        val park = parksList[position]

        // Assuming you have an image URL in the Park object (you need to define how to get it)
        // For example, if you have a property called `images` in the Park class
        val imageUrl = park.images.firstOrNull()?.url // Replace with the actual property that holds the image URL

        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(R.drawable.baseline_downloading_24) // Placeholder while loading
            .error(R.drawable.no_image_available) // Fallback image if loading fails
            .into(holder.thumbnailImageView)

        // Set the park name
        holder.parkNameTextView.text = park.fullName // Use the fullName property from the Park object

        // Set the click listener for the item
        holder.itemView.setOnClickListener {
            onParkClick(park) // Use the onParkClick callback
        }
    }

    // Return the size of the dataset
    override fun getItemCount(): Int {
        return parksList.size
    }

    // Update the dataset and refresh the RecyclerView
    fun updateData(newParks: List<Park>) {
        parksList = newParks
        notifyDataSetChanged()
    }
}