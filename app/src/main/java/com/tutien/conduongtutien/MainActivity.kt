package com.tutien.conduongtutien

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var player: Player
    private lateinit var gameView: GameView
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        player = SaveManager.loadGame(this)
        gameView = GameView(this, player)
        setContentView(gameView)

        runnable = object : Runnable {
            override fun run() {
                gameView.update()
                handler.postDelayed(this, 33L)
            }
        }
        handler.post(runnable)
    }

    override fun onPause() {
        super.onPause()
        SaveManager.saveGame(this, player)
        handler.removeCallbacks(runnable)
    }

    override fun onResume() {
        super.onResume()
        handler.post(runnable)
    }
}
