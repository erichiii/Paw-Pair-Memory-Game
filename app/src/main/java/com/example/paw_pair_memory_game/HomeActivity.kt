package com.example.paw_pair_memory_game

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        MusicManager.start(this, volume = 1.0f)

        val easyButton = findViewById<ImageButton>(R.id.easy_button)
        val mediumButton = findViewById<ImageButton>(R.id.medium_button)
        val hardButton = findViewById<ImageButton>(R.id.hard_button)

        easyButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("level", "easy")
            startActivity(intent)
        }

        mediumButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("level", "medium")
            startActivity(intent)
        }

        hardButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("level", "hard")
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        MusicManager.resume()
        MusicManager.fadeTo(1.0f, 500)
    }

    override fun onPause() {
        super.onPause()
        if (!isChangingConfigurations) {
            MusicManager.pause()
        }
    }
}
