package com.example.trailblaze

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.R

class ParksAdapter(private var parksList: List<Park>) : RecyclerView.Adapter<ParksAdapter.ParkViewHolder>() {

    class ParkViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.parkName)
        val descriptionTextView: TextView = view.findViewById(R.id.parkDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParkViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_park, parent, false)
        return ParkViewHolder(view)
    }

    override fun onBindViewHolder(holder: ParkViewHolder, position: Int) {
        val park = parksList[position]
        holder.nameTextView.text = park.fullName
        holder.descriptionTextView.text = park.description
    }

    override fun getItemCount(): Int {
        return parksList.size
    }

    // Method to update the data in the adapter
    fun updateData(newParks: List<Park>) {
        parksList = newParks
        notifyDataSetChanged()
    }
}
