package com.example.trailblaze.ui.Map

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.R
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.example.trailblaze.BuildConfig.PLACES_API_KEY
import com.google.android.libraries.places.api.net.kotlin.fetchPhotoRequest

class MapBottomSheetAdapter(val items : MutableList<Place>): RecyclerView.Adapter<MapBottomSheetAdapter.MapBottomSheetHolder>() {


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
        holder.titleTv.text = currentItem.displayName
        holder.addressTv.text = currentItem.formattedAddress
    }
}