package com.example.trailblaze.ui.parks

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trailblaze.R

class TimeRecordAdapter(private val timeRecords: MutableList<TimeRecord>) :
    RecyclerView.Adapter<TimeRecordAdapter.TimeRecordViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeRecordViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_time_record, parent, false)
        return TimeRecordViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeRecordViewHolder, position: Int) {
        val timeRecord = timeRecords[position]
        holder.parkNameTextView.text = timeRecord.parkName
        holder.elapsedTimeTextView.text = timeRecord.elapsedTime

        // Load the image using Glide
        timeRecord.imageUrl?.let { imageUrl ->
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.baseline_downloading_24) // Placeholder while loading
                .error(R.drawable.no_image_available) // Fallback image if loading fails
                .into(holder.parkImageView)
        }
    }

    override fun getItemCount(): Int = timeRecords.size

    // Method to update data in the adapter
    fun updateData(newTimeRecords: List<TimeRecord>) {
        timeRecords.clear()
        timeRecords.addAll(newTimeRecords)
        notifyDataSetChanged()
    }

    class TimeRecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val parkNameTextView: TextView = itemView.findViewById(R.id.parkNameTextView)
        val elapsedTimeTextView: TextView = itemView.findViewById(R.id.elapsedTimeTextView)
        val parkImageView: ImageView = itemView.findViewById(R.id.thumbnailImageView) // New ImageView for park image
    }
}

