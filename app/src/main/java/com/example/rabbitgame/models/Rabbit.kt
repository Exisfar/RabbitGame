package com.example.rabbitgame.models

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect

class Rabbit(
    var x: Float,
    var y: Float,
    val width: Int,
    val height: Int,
    private val sprite: Bitmap
) {
    private val speed = 15f
    var isMovingLeft = false
    var isMovingRight = false

    val bounds: Rect
        get() = Rect(x.toInt(), y.toInt(), (x + width).toInt(), (y + height).toInt())

    fun update(screenWidth: Int) {
        when {
            isMovingLeft -> x = (x - speed).coerceAtLeast(0f)
            isMovingRight -> x = (x + speed).coerceAtMost(screenWidth - width.toFloat())
        }
    }

    fun draw(canvas: Canvas) {
        canvas.drawBitmap(sprite, null, bounds, null)
    }
}