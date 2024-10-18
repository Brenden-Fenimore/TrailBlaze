package com.example.trailblaze.ui.achievements

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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

            fun bind(category: AchievementCategory) {
                title.text = category.title

                //set the default badges
                badge1.setImageResource(category.badgeResourceIds.getOrNull(0) ?: R.drawable.default_badge)
                badge1.setOnClickListener {
                    showTooltip(badge1, category.tooltipTexts[0])
                }

                badge2.setImageResource(category.badgeResourceIds.getOrNull(1) ?: R.drawable.default_badge)
                badge2.setOnClickListener {
                    showTooltip(badge2, category.tooltipTexts[1])
                }

                badge3.setImageResource(category.badgeResourceIds.getOrNull(2) ?: R.drawable.default_badge)
                badge3.setOnClickListener {
                    showTooltip(badge3, category.tooltipTexts[2])
                }
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.achievement_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.bind(category)
    }

    private fun showTooltip(anchor: View, message: String) {
        val inflater = LayoutInflater.from(anchor.context)
        val popupView = inflater.inflate(R.layout.popup_tooltip, null)
        val popupText = popupView.findViewById<TextView>(R.id.popup_text)
        popupText.text = message

        val popupWindow = PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        popupWindow.isFocusable = true
        popupWindow.isOutsideTouchable = true

        // Show the popup window
        popupWindow.showAsDropDown(anchor)

        // Dismiss the popup after a delay (optional)
        popupView.postDelayed({
            popupWindow.dismiss()
        }, 2000) // Dismiss after 2 seconds
    }


    override fun getItemCount(): Int {
        return categories.size
    }
}