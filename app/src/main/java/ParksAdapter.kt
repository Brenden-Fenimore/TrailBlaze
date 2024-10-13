package com.example.trailblaze

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.R

// Adapter class for binding park data to RecyclerView items
class ParksAdapter(private var parksList: List<Park>) : RecyclerView.Adapter<ParksAdapter.ParkViewHolder>() {

    // ViewHolder for holding views for each item
    class ParkViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.parkName)                   // TextView for park name
        val descriptionTextView: TextView = view.findViewById(R.id.parkDescription)    // TextView for park description
        val latitudeTextView: TextView = view.findViewById(R.id.parkLatitude)
        val longitudeTextView: TextView = view.findViewById(R.id.parkLongitude)
    }

    // Inflate the layout for each RecyclerView item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParkViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_park, parent, false)
        return ParkViewHolder(view)
    }

    // Bind data to the views for each item
    override fun onBindViewHolder(holder: ParkViewHolder, position: Int) {
        val park = parksList[position]
        holder.nameTextView.text = park.fullName
        holder.descriptionTextView.text = park.description
        holder.latitudeTextView.text = "Latitude: ${park.latitude}"
        holder.longitudeTextView.text = "Longitude: ${park.longitude}"
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
