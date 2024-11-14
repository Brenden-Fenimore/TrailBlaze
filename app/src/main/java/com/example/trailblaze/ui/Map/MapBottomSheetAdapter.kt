package com.example.trailblaze.ui.Map

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.R
import com.example.trailblaze.databinding.ItemMapPlaceBinding
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.kotlin.awaitFetchPhoto

class MapBottomSheetAdapter(val items : MutableList<Place>, placesClient: PlacesClient,val onClick : (String) -> Unit): RecyclerView.Adapter<MapBottomSheetAdapter.MapBottomSheetHolder>() {

val placesClient = placesClient
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MapBottomSheetHolder {
        val binding = ItemMapPlaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_map_place, parent, false)
        return MapBottomSheetHolder(binding)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class MapBottomSheetHolder(val binding : ItemMapPlaceBinding): RecyclerView.ViewHolder(binding.root)
    {
//        val titleTv : TextView
//        val addressTv : TextView
//        val imageIv : ImageView
//        init {
//            titleTv = itemView.findViewById(R.id.item_map_place_title)
//            addressTv = itemView.findViewById(R.id.item_map_place_address)
//            imageIv = itemView.findViewById(R.id.item_map_place_image)
//        }
        init {
            binding.root.setOnClickListener {
                onClick(binding.itemMapPlaceId.text.toString())
            }
        }
    }

    override fun onBindViewHolder(holder: MapBottomSheetHolder, position: Int) {
        val currentItem = items[position]
        if(currentItem.photoMetadatas.size !=0) {
            placesClient.fetchPhoto(FetchPhotoRequest.builder(currentItem.photoMetadatas[0]).build())
                .addOnSuccessListener { response ->
                    holder.binding.itemMapPlaceImage.setImageBitmap(response.bitmap)
                }
        }
        holder.binding.itemMapPlaceTitle.text = currentItem.displayName
        holder.binding.itemMapPlaceAddress.text = currentItem.formattedAddress
        holder.binding.itemMapPlaceId.text = currentItem.id?.toString() ?: "0"
    }
}