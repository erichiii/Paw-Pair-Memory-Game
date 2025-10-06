package com.example.paw_pair_memory_game

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var imageViews: MutableList<ImageView>
    private lateinit var cardIcons: List<Int>
    private val isFlipped = BooleanArray(12) { false }
    private val cardBack = R.drawable.card_back
    private val cardFront = R.drawable.card_outline

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val gridLayout = findViewById<androidx.gridlayout.widget.GridLayout>(R.id.gridLayout)
        imageViews = mutableListOf()

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

        cardIcons = (icons + icons).shuffled()
        val violetColor = Color.parseColor("#9C27B0")

        for (i in imageViews.indices) {
            val imageView = imageViews[i]
            imageView.setBackgroundResource(cardBack)
            imageView.tag = cardIcons[i]

            imageView.setOnClickListener {
                if (!isFlipped[i]) {
                    flipCard(imageView, i, violetColor)
                }
            }
        }
    }

    private fun flipCard(imageView: ImageView, index: Int, color: Int) {
        isFlipped[index] = true

        val oa1 = ObjectAnimator.ofFloat(imageView, "rotationY", 0f, 90f)
        val oa2 = ObjectAnimator.ofFloat(imageView, "rotationY", -90f, 0f)

        oa1.duration = 250
        oa2.duration = 250

        oa1.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                imageView.setBackgroundResource(cardFront)
                val iconRes = imageView.tag as Int
                val drawable = ContextCompat.getDrawable(applicationContext, iconRes)?.mutate()
                drawable?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
                imageView.setImageDrawable(drawable)
                oa2.start()
            }
        })

        oa1.start()
    }
}
