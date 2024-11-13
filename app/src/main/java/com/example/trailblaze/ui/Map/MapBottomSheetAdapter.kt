package com.example.trailblaze.ui.Map

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trailblaze.R
import com.example.trailblaze.nps.Park
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.kotlin.awaitFetchPhoto

class MapBottomSheetAdapter(
    private var items: MutableList<LocationItem> = mutableListOf(),
    private val placesClient: PlacesClient,
    private val onItemClick: (LocationItem) -> Unit
) : RecyclerView.Adapter<MapBottomSheetAdapter.MapBottomSheetHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MapBottomSheetHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_map_place, parent, false)
        return MapBottomSheetHolder(view)
    }

    override fun getItemCount() = items.size

    class MapBottomSheetHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTv: TextView = itemView.findViewById(R.id.item_map_place_title)
        val addressTv: TextView = itemView.findViewById(R.id.item_map_place_address)
        val imageIv: ImageView = itemView.findViewById(R.id.item_map_place_image)
    }

    override fun onBindViewHolder(holder: MapBottomSheetHolder, position: Int) {
        when (val item = items[position]) {
            is LocationItem.PlaceItem -> bindPlaceItem(holder, item.place)
            is LocationItem.ParkItem -> bindParkItem(holder, item.park)
        }

        holder.itemView.setOnClickListener { onItemClick(items[position]) }
    }

    private fun bindPlaceItem(holder: MapBottomSheetHolder, place: Place) {
        holder.titleTv.text = place.displayName
        holder.addressTv.text = place.formattedAddress

        if (place.photoMetadatas?.isNotEmpty() == true) {
            placesClient.fetchPhoto(FetchPhotoRequest.builder(place.photoMetadatas[0]).build())
                .addOnSuccessListener { response ->
                    holder.imageIv.setImageBitmap(response.bitmap)
                }
        } else {
            holder.imageIv.setImageResource(R.drawable.no_image_available)
        }
    }

    private fun bindParkItem(holder: MapBottomSheetHolder, park: Park) {
        holder.titleTv.text = park.fullName
        holder.addressTv.text = park.addresses?.firstOrNull()?.line1 ?: ""

        Glide.with(holder.itemView.context)
            .load(park.images?.firstOrNull()?.url)
            .placeholder(R.drawable.baseline_downloading_24)
            .error(R.drawable.no_image_available)
            .into(holder.imageIv)
    }

    fun updateItems(newItems: List<LocationItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}