package com.example.rabbitgame.models

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import kotlin.random.Random

class Carrot(
    var x: Float,
    var y: Float,
    val width: Int,
    val height: Int,
    private val sprite: Bitmap,
    private val speed: Float = 8f
) {
    var isActive = true

    val bounds: Rect
        get() = Rect(x.toInt(), y.toInt(), (x + width).toInt(), (y + height).toInt())

    fun update(screenHeight: Int): Boolean {
        y += speed
        if (y > screenHeight) {
            isActive = false
            return false
        }
        return true
    }

    fun draw(canvas: Canvas) {
        if (isActive) {
            canvas.drawBitmap(sprite, null, bounds, null)
        }
    }

    fun checkCollision(rabbitBounds: Rect): Boolean {
        return isActive && Rect.intersects(bounds, rabbitBounds)
    }
}