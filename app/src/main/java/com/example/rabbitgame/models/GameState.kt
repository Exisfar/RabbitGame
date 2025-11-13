package com.example.rabbitgame.models

data class GameState(
    var score: Int = 0,
    var lives: Int = 3,
    var isGameOver: Boolean = false,
    var isPaused: Boolean = false
) {
    fun reset() {
        score = 0
        lives = 3
        isGameOver = false
        isPaused = false
    }

    fun addScore(points: Int) {
        score += points
    }

    fun loseLife() {
        lives--
        if (lives <= 0) {
            isGameOver = true
        }
    }
}