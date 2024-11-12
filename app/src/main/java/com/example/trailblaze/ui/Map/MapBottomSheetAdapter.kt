package com.example.trailblaze.ui.Map

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.R
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.kotlin.awaitFetchPhoto

class MapBottomSheetAdapter(val items : MutableList<Place>, placesClient: PlacesClient): RecyclerView.Adapter<MapBottomSheetAdapter.MapBottomSheetHolder>() {

val placesClient = placesClient

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MapBottomSheetHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_map_place, parent, false)
        return MapBottomSheetHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class MapBottomSheetHolder(itemView: View): RecyclerView.ViewHolder(itemView)
    {
        val titleTv : TextView
        val addressTv : TextView
        val imageIv : ImageView
        init {
            titleTv = itemView.findViewById(R.id.item_map_place_title)
            addressTv = itemView.findViewById(R.id.item_map_place_address)
            imageIv = itemView.findViewById(R.id.item_map_place_image)
        }
    }

    override fun onBindViewHolder(holder: MapBottomSheetHolder, position: Int) {
        val currentItem = items[position]
        if(currentItem.photoMetadatas.size !=0) {
            placesClient.fetchPhoto(FetchPhotoRequest.builder(currentItem.photoMetadatas[0]).build())
                .addOnSuccessListener { response ->
                    holder.imageIv.setImageBitmap(response.bitmap)
                }
        }
        holder.titleTv.text = currentItem.displayName
        holder.addressTv.text = currentItem.formattedAddress
    }
}