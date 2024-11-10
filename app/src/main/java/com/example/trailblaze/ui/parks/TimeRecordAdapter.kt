package com.example.trailblaze.ui.parks

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trailblaze.R

class TimeRecordAdapter(
    private val timeRecords: MutableList<TimeRecord>,
    private val onItemClick: (TimeRecord) -> Unit
) : RecyclerView.Adapter<TimeRecordAdapter.TimeRecordViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeRecordViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_time_record, parent, false)
        return TimeRecordViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeRecordViewHolder, position: Int) {
        val timeRecord = timeRecords[position]
        holder.parkNameTextView.text = timeRecord.parkName
        holder.elapsedTimeTextView.text = timeRecord.elapsedTime
        holder.dateTextView.text = timeRecord.date // Bind the date to the new TextView

        // Load the image using Glide
        if (timeRecord.imageUrl != null) {
            Glide.with(holder.itemView.context)
                .load(timeRecord.imageUrl)
                .placeholder(R.drawable.baseline_downloading_24) // Placeholder while loading
                .error(R.drawable.no_image_available) // Fallback image if loading fails
                .into(holder.parkImageView)
        } else {
            holder.parkImageView.setImageResource(R.drawable.no_image_available) // Set fallback image if no URL
        }

        // Set the click listener for the item
        holder.itemView.setOnClickListener {
            onItemClick(timeRecord) // Call the click listener with the clicked timeRecord
        }
    }

    override fun getItemCount(): Int = timeRecords.size

    // Method to update data in the adapter
    fun updateData(newTimeRecords: List<TimeRecord>) {
        timeRecords.clear()
        timeRecords.addAll(newTimeRecords)
        notifyDataSetChanged() // Consider switching to DiffUtil for efficiency on larger datasets
    }

    // Optional: Method to remove a specific item
    fun removeItem(position: Int) {
        if (position >= 0 && position < timeRecords.size) {
            timeRecords.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    class TimeRecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val parkNameTextView: TextView = itemView.findViewById(R.id.parkNameTextView)
        val elapsedTimeTextView: TextView = itemView.findViewById(R.id.elapsedTimeTextView)
        val parkImageView: ImageView = itemView.findViewById(R.id.thumbnailImageView) // New ImageView for park image
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView) // Add this line for dateTextView
    }
}