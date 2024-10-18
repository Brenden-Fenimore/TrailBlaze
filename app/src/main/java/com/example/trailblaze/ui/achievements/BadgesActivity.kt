package com.example.trailblaze.ui.achievements

import android.os.Bundle
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.R

class BadgesActivity : AppCompatActivity() {

    private lateinit var badgesRecyclerView: RecyclerView
    private lateinit var sash: FrameLayout
    private lateinit var badgesAdapter: BadgesAdapter
    private val badges: List<Badge> = listOf(
        Badge("safetyexpert", "Safety Expert", R.drawable.safetyexpert),
        Badge("trailblaze", "TrailBlazer", R.drawable.trailblaze_logo),
        Badge("mountainclimber", "MountainClimber", R.drawable.mountainclimber),
        Badge("trekker", "Trekker", R.drawable.trekker),
        Badge("hiker", "Hiker", R.drawable.hhiker),
        Badge("weekendwarrior", "Weekend Warrior", R.drawable.weekendwarrior),
        Badge("dailyadventurer", "Daily Adventurer", R.drawable.dailyadventurer),
        Badge("conqueror", "Conqueror", R.drawable.conqueror),
        Badge("explorer", "Explorer", R.drawable.explorer),
        Badge("trailmaster", "Trailmaster", R.drawable.trailmaster),
        Badge("socialbutterfly", "Socialbutterfly", R.drawable.socialbutterfly),
        Badge("teamplayer", "Teleamplayer", R.drawable.teamplayer),
        Badge("communitybuilder", "Community Builder", R.drawable.communitybuilder),
        Badge("wildlifewatcher", "Wildlifewatcher", R.drawable.wildlifewatcher),
        Badge("photographer", "Photographer", R.drawable.photographer),
        Badge("goalsetter", "Goalsetter", R.drawable.goalsetter),
        Badge("badgecollector", "Badge Collector", R.drawable.badgecollector),
        Badge("leaderboard", "Leaderboard", R.drawable.leaderboard),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_badges)

        findViewById<ImageButton>(R.id.chevron_left).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        badgesRecyclerView = findViewById(R.id.recycler_view_badges)
        sash = findViewById(R.id.sash)

        badgesAdapter = BadgesAdapter(badges, itemClickListener = { badge ->
            Toast.makeText(this, "Clicked on: ${badge.name}", Toast.LENGTH_SHORT).show()
        })

        badgesRecyclerView.adapter = badgesAdapter
        badgesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Set the drag listener on the sash
        sash.setOnDragListener(sashDragListener)
    }

    private val sashDragListener = View.OnDragListener { v, event ->
        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                true
            }

            DragEvent.ACTION_DRAG_ENTERED -> {
                // Optionally change the appearance of the sash (e.g., highlight)
                true
            }

            DragEvent.ACTION_DRAG_EXITED -> {
                // Optionally revert the appearance
                true
            }

            DragEvent.ACTION_DROP -> {
                val draggedView = event.localState as View

                // Get drop coordinates
                val x = event.x.toInt()
                val y = event.y.toInt()

                // Check if draggedView has a parent and remove it from the parent
                (draggedView.parent as? ViewGroup)?.removeView(draggedView)

                // Set the layout parameters for the badge
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

                // Set the position of the badge
                draggedView.layoutParams = params
                draggedView.scaleX = 0.1f
                draggedView.scaleY = 0.1f
                draggedView.x = x.toFloat()
                draggedView.y = y.toFloat()
                draggedView.visibility = View.VISIBLE

                // Add the dragged badge to the sash
                (v as FrameLayout).addView(draggedView)

                // Set an OnTouchListener for dragging the badge again
                draggedView.setOnTouchListener { view, touchEvent ->
                    when (touchEvent.action) {
                        MotionEvent.ACTION_DOWN -> {
                            // Start dragging the badge
                            val dragShadowBuilder = View.DragShadowBuilder(view)
                            view.startDragAndDrop(null, dragShadowBuilder, view, 0)
                            true
                        }
                        else -> false
                    }
                }

                true
            }

            else -> false
        }
    }
}