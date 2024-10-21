package com.example.trailblaze.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View


class SwishBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        color = Color.parseColor("#4C662B")
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
}