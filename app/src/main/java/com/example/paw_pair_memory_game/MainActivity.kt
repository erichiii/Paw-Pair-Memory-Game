package com.example.paw_pair_memory_game

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val gridLayout = findViewById<androidx.gridlayout.widget.GridLayout>(R.id.gridLayout)
        val imageViews = mutableListOf<ImageView>()

        for (i in 0 until gridLayout.childCount) {
            imageViews.add(gridLayout.getChildAt(i) as ImageView)
        }

        val icons = mutableListOf(
            android.R.drawable.ic_dialog_email,
            android.R.drawable.ic_dialog_info,
            android.R.drawable.ic_dialog_dialer,
            android.R.drawable.ic_dialog_map,
            android.R.drawable.ic_dialog_alert,
            android.R.drawable.ic_menu_camera
        )

        // Create pairs and shuffle
        val cardIcons = (icons + icons).shuffled()

        val violetColor = Color.parseColor("#9C27B0")

        // Assign icons to ImageViews and apply color tint
        for (i in imageViews.indices) {
            val drawable = ContextCompat.getDrawable(this, cardIcons[i])
            drawable?.setColorFilter(violetColor, PorterDuff.Mode.SRC_IN)
            imageViews[i].setImageDrawable(drawable)
        }
    }
}
