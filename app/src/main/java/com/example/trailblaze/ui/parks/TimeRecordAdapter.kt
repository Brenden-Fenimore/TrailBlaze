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
        holder.dateTextView.text = timeRecord.date

        if (timeRecord.imageUrl != null) {
            Glide.with(holder.itemView.context)
                .load(timeRecord.imageUrl)
                .placeholder(R.drawable.baseline_downloading_24)
                .error(R.drawable.no_image_available)
                .into(holder.parkImageView)
        } else {
            holder.parkImageView.setImageResource(R.drawable.no_image_available)
        }

        holder.itemView.setOnClickListener {
            if (timeRecord.place == true) {
                // For places, use the placeId stored in parkCode
                val modifiedRecord = timeRecord.copy(placeId = timeRecord.parkCode)
                onItemClick(modifiedRecord)
            } else {
                onItemClick(timeRecord)
            }
        }
    }

    override fun getItemCount(): Int = timeRecords.size

    fun updateData(newTimeRecords: List<TimeRecord>) {
        val processedRecords = newTimeRecords.map { record ->
            if (record.place == true) {
                record.copy(parkCode = record.placeId ?: "")
            } else {
                record
            }
        }
        timeRecords.clear()
        timeRecords.addAll(processedRecords)
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        if (position >= 0 && position < timeRecords.size) {
            timeRecords.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    class TimeRecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val parkNameTextView: TextView = itemView.findViewById(R.id.parkNameTextView)
        val elapsedTimeTextView: TextView = itemView.findViewById(R.id.elapsedTimeTextView)
        val parkImageView: ImageView = itemView.findViewById(R.id.thumbnailImageView)
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
    }
}