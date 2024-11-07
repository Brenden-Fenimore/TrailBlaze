package com.example.trailblaze.ui.achievements

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.R

// Adapter for displaying achievement categories in a RecyclerView
class AchievementsAdapter(private val categories: List<AchievementCategory>) :
    RecyclerView.Adapter<AchievementsAdapter.ViewHolder>() {

    // Inner class to represent each item (card) in the RecyclerView
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // UI components for the achievement category
        val title: TextView = view.findViewById(R.id.category_title) // TextView for the category title
        val badge1: ImageView = view.findViewById(R.id.badge_1) // ImageView for the first badge
        val badge2: ImageView = view.findViewById(R.id.badge_2) // ImageView for the second badge
        val badge3: ImageView = view.findViewById(R.id.badge_3) // ImageView for the third badge

        // Function to bind data from an AchievementCategory to the UI components
        fun bind(category: AchievementCategory) {
            title.text = category.title // Set the category title

            // Set the default badges; use a default badge if the resource is not available
            badge1.setImageResource(category.badgeResourceIds.getOrNull(0) ?: R.drawable.default_badge)
            badge1.setOnClickListener {
                // Show tooltip for the first badge when clicked
                showTooltip(badge1, category.tooltipTexts[0])
            }

            badge2.setImageResource(category.badgeResourceIds.getOrNull(1) ?: R.drawable.default_badge)
            badge2.setOnClickListener {
                // Show tooltip for the second badge when clicked
                showTooltip(badge3, category.tooltipTexts[2])
            }
        }
    }

    // Create a new ViewHolder instance for the RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate the layout for each achievement card
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.achievement_card, parent, false)
        // Return a new ViewHolder with the inflated view
        return ViewHolder(view)
    }

    // Bind the data to the ViewHolder at the specified position
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Get the achievement category for the current position in the RecyclerView
        val category = categories[position]

        // Bind the category data to the ViewHolder
        holder.bind(category)
    }

    // Function to show a tooltip message at a specified anchor view
    private fun showTooltip(anchor: View, message: String) {
        // Get a LayoutInflater instance from the anchor's context
        val inflater = LayoutInflater.from(anchor.context)

        // Inflate the layout for the tooltip popup
        val popupView = inflater.inflate(R.layout.popup_tooltip, null)

        // Find the TextView in the popup layout and set the tooltip message
        val popupText = popupView.findViewById<TextView>(R.id.popup_text)
        popupText.text = message // Set the text of the tooltip to the provided message

        // Create a PopupWindow to display the tooltip
        val popupWindow = PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        popupWindow.isFocusable = true // Allow the popup to receive focus
        popupWindow.isOutsideTouchable = true // Allow dismissing the popup by tapping outside of it

        // Show the popup window below the anchor view
        popupWindow.showAsDropDown(anchor)

        // Dismiss the popup after a delay (optional)
        popupView.postDelayed({
            popupWindow.dismiss() // Dismiss the popup window after the specified delay
        }, 2000) // Dismiss after 2 seconds
    }


    override fun getItemCount(): Int {
        return categories.size
    }
}