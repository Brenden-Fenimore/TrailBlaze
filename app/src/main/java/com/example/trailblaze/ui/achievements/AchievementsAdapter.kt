package com.example.trailblaze.ui.achievements

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.R

class AchievementsAdapter(private val categories: List<AchievementCategory>) :
    RecyclerView.Adapter<AchievementsAdapter.ViewHolder>() {

        //create an inner class to cycle thru each card
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.category_title)
        val badge1: ImageView = view.findViewById(R.id.badge_1)
        val badge2: ImageView = view.findViewById(R.id.badge_2)
        val badge3: ImageView = view.findViewById(R.id.badge_3)
        val progressBar: ProgressBar = view.findViewById(R.id.category_progress_bar)
    }

    //infate the image card
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.achievement_card, parent, false)
        return ViewHolder(view)
    }

    //pull the categories
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.title.text = category.title
        holder.progressBar.progress = category.progress

        //set the default badges
        holder.badge1.setImageResource(category.badgeResourceIds.getOrNull(0) ?: R.drawable.default_badge)
        holder.badge2.setImageResource(category.badgeResourceIds.getOrNull(1) ?: R.drawable.default_badge)
        holder.badge3.setImageResource(category.badgeResourceIds.getOrNull(2) ?: R.drawable.default_badge)
    }

    override fun getItemCount(): Int {
        return categories.size
    }
}