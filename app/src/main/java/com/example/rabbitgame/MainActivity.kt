package com.example.rabbitgame

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Window
import android.view.WindowManager
import android.widget.Button

class MainActivity : Activity() {

    private lateinit var gameView: GameView
    private lateinit var gameThread: Thread
    private var isRunning = false
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 设置全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_main)

        gameView = findViewById(R.id.gameView)

        // 控制按钮
        findViewById<Button>(R.id.btnRestart).setOnClickListener {
            gameView.restartGame()
        }

        findViewById<Button>(R.id.btnPause).setOnClickListener {
            gameView.pauseGame()
        }

        findViewById<Button>(R.id.btnResume).setOnClickListener {
            gameView.resumeGame()
        }
    }

    override fun onResume() {
        super.onResume()
        startGameLoop()
    }

    override fun onPause() {
        super.onPause()
        stopGameLoop()
    }

    private fun startGameLoop() {
        isRunning = true
        gameThread = Thread {
            while (isRunning) {
                try {
                    Thread.sleep(16) // 约60FPS
                    handler.post {
                        gameView.update()
                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
        gameThread.start()
    }

    private fun stopGameLoop() {
        isRunning = false
        try {
            gameThread.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
}