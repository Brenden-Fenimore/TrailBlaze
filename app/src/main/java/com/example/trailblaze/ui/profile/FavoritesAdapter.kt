package com.example.trailblaze.ui.profile

import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trailblaze.R
import com.example.trailblaze.ui.Map.LocationItem

class FavoritesAdapter(
    private var items: List<LocationItem>,
    private val onItemClick: (LocationItem) -> Unit
) : RecyclerView.Adapter<FavoritesAdapter.LocationViewHolder>() {

    class LocationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val thumbnailImageView: ImageView = view.findViewById(R.id.thumbnailImageView)
        val nameTextView: TextView = view.findViewById(R.id.parkNameTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_park, parent, false)
        return LocationViewHolder(view)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val item = items[position]

        when (item) {
            is LocationItem.ParkItem -> {
                val park = item.park
                holder.nameTextView.text = park.fullName

                val imageUrl = park.images.firstOrNull()?.url
                Glide.with(holder.itemView.context)
                    .load(imageUrl)
                    .placeholder(R.drawable.baseline_downloading_24)
                    .error(R.drawable.no_image_available)
                    .into(holder.thumbnailImageView)
            }

            is LocationItem.PlaceItem -> {
                val place = item.place
                holder.nameTextView.text = place.displayName?.toString()

                // Handle Place photo
                place.photoMetadatas?.firstOrNull()?.let { metadata ->
                    val photoRequest = FetchPhotoRequest.builder(metadata)
                        .setMaxWidth(500)
                        .setMaxHeight(500)
                        .build()

                    Places.createClient(holder.itemView.context)
                        .fetchPhoto(photoRequest)
                        .addOnSuccessListener { response ->
                            holder.thumbnailImageView.setImageBitmap(response.bitmap)
                        }
                        .addOnFailureListener {
                            holder.thumbnailImageView.setImageResource(R.drawable.no_image_available)
                        }
                } ?: holder.thumbnailImageView.setImageResource(R.drawable.no_image_available)
            }
        }

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<LocationItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}