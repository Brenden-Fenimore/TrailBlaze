package com.example.trailblaze.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.trailblaze.R


class SwishBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        //Retrieve colorPrimary from the current theme
        color = getThemeColor(context, com.google.maps.android.R.attr.colorPrimary)
        isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height * 1.0f

        // Creates a Path to define the swish curve
        val path = Path().apply {
            moveTo(0f, height)
            quadTo(width / 2, 0f, width, height)
            lineTo(width, 0f)
            lineTo(0f, 0f)
            close()
        }

        // Draws the curve on the canvas
        canvas.drawPath(path, paint)
    }

    // Helper function to retrieve the current theme's color for the swish banner
    private fun getThemeColor(context: Context, attr: Int): Int {
        //Access the current theme and try to fetch the color associated with 'attr'
        val typedArray = context.theme.obtainStyledAttributes(intArrayOf(attr))
        // Retrieve the fallback color from the obtained attributes.
        val color = typedArray.getColor(
            0, // Index of the attribute (only one attribute passed, so index is 0)
            ContextCompat.getColor(context, R.color.primary_day_color)) // Fallback color
        // Recycle the TypedArray to free up memory resources.
        typedArray.recycle()
        // Return the resolved color,
        return color
    }
}