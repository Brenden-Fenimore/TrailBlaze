package com.example.trailblaze.nps

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.R
import com.example.trailblaze.ui.parks.ParkDetailActivity

// Adapter class for binding park data to RecyclerView items
class ParksAdapter(var parksList: List<Park>, private val onParkClick: (Park) -> Unit) : RecyclerView.Adapter<ParksAdapter.ParkViewHolder>() {

    // ViewHolder for holding views for each item
    class ParkViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val parkNameTextView: TextView = view.findViewById(R.id.parkNameTextView)
    }

    // Inflate the layout for each RecyclerView item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParkViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_park, parent, false)
        return ParkViewHolder(view)
    }

    // Bind data to the views for each item
    override fun onBindViewHolder(holder: ParkViewHolder, position: Int) {
        val park = parksList[position]
        holder.parkNameTextView.text = park.fullName

        // Set click listener for each item
        holder.itemView.setOnClickListener {
            onParkClick(park) // Pass the actual park object
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