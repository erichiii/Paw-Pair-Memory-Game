package com.example.paw_pair_memory_game

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val easyButton = findViewById<Button>(R.id.easy_button)
        val mediumButton = findViewById<Button>(R.id.medium_button)
        val hardButton = findViewById<Button>(R.id.hard_button)

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
}
