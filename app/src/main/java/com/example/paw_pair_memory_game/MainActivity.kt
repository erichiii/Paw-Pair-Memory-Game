package com.example.paw_pair_memory_game

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.gridlayout.widget.GridLayout

class MainActivity : AppCompatActivity() {

    private lateinit var imageViews: MutableList<ImageView>
    private lateinit var cardIcons: List<Int>
    private lateinit var isFlipped: BooleanArray
    private val cardBack = R.drawable.card_back_1
    private val cardFront = R.drawable.card_front_2

    private val flippedCards = mutableListOf<ImageView>()
    private var isChecking = false

    private lateinit var timerTextView: TextView
    private lateinit var scoreTextView: TextView

    private var score = 0
    private var pairsFound = 0
    private var timeLeft = 0L
    private lateinit var countDownTimer: CountDownTimer
    private lateinit var level: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        level = intent.getStringExtra("level") ?: "easy"
        val gridLayout = findViewById<GridLayout>(R.id.gridLayout)
        timerTextView = findViewById(R.id.timer_textview)
        scoreTextView = findViewById(R.id.score_textview)
        scoreTextView.text = getString(R.string.score_text, score)

        val (rows, cols, timeLimit) = when (level) {
            "easy" -> Triple(2, 2, 30000L)
            "medium" -> Triple(4, 3, 60000L)
            "hard" -> Triple(5, 4, 90000L)
            else -> Triple(2, 2, 30000L)
        }

        startTimer(timeLimit)

        gridLayout.rowCount = rows
        gridLayout.columnCount = cols
        val numCards = rows * cols
        isFlipped = BooleanArray(numCards)

        val allIcons = mutableListOf(
            R.drawable.smiling_cat,
            R.drawable.owo,
            R.drawable.shocked,
            R.drawable.eyy_cat,
            R.drawable.good_boy_cat,
            R.drawable.laughing_car,
            R.drawable.startled_car,
            R.drawable.stressed_cat,
            R.drawable.judgmental_car,
            R.drawable.call_center_car
        )
        allIcons.shuffle()
        val icons = allIcons

        cardIcons = (icons.take(numCards / 2) + icons.take(numCards / 2)).shuffled()
        imageViews = mutableListOf()

        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels

        val gridMarginInDp = 16f
        val gridMarginInPixels = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, gridMarginInDp, displayMetrics
        ).toInt()

        val cardMarginInDp = 8f
        val cardMarginInPixels = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, cardMarginInDp, displayMetrics
        ).toInt()

        val availableWidth = screenWidth - (2 * gridMarginInPixels)

        val calculatedCardWidth = (availableWidth - ((cols - 1) * cardMarginInPixels)) / cols

        val desiredCardSizeInDp = 100f
        val desiredCardSizeInPixels = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, desiredCardSizeInDp, displayMetrics
        ).toInt()

        val cardSize = desiredCardSizeInPixels.coerceAtMost(calculatedCardWidth)

        val initialElevationInDp = 8f
        val initialElevationInPixels = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, initialElevationInDp, displayMetrics
        )

        for (i in 0 until numCards) {
            val imageView = ImageView(this)
            val params = GridLayout.LayoutParams()
            params.width = cardSize
            params.height = cardSize
            imageView.layoutParams = params
            imageView.elevation = initialElevationInPixels

            imageView.setBackgroundResource(cardBack)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.setPadding(0, 0, 0, 0)
            imageView.setImageDrawable(null)
            imageView.tag = cardIcons[i]
            imageViews.add(imageView)
            gridLayout.addView(imageView)

            imageView.setOnClickListener {
                if (!isChecking && !isFlipped[i] && flippedCards.size < 2) {
                    flipCard(imageView, i)
                }
            }
        }
    }

    private fun startTimer(timeLimit: Long) {
        countDownTimer = object : CountDownTimer(timeLimit, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeft = millisUntilFinished / 1000
                timerTextView.text = getString(R.string.timer_text, timeLeft.toInt())
            }

            override fun onFinish() {
                timerTextView.text = getString(R.string.timer_text, 0)
                endGame(false)
            }
        }.start()
    }

    private fun flipCard(imageView: ImageView, index: Int) {
        isFlipped[index] = true
        flippedCards.add(imageView)

        val oa1 = ObjectAnimator.ofFloat(imageView, "rotationY", 0f, 90f)
        val oa2 = ObjectAnimator.ofFloat(imageView, "rotationY", -90f, 0f)

        oa1.duration = 150
        oa2.duration = 150

        oa1.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                imageView.setBackgroundResource(cardFront)
                val iconRes = imageView.tag as Int
                val drawable = ContextCompat.getDrawable(applicationContext, iconRes)?.mutate()
                imageView.setImageDrawable(drawable)
                oa2.start()
            }
        })

        oa2.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                if (flippedCards.size == 2) {
                    isChecking = true
                    checkForMatch()
                }
            }
        })

        oa1.start()
    }

    private fun checkForMatch() {
        val card1 = flippedCards[0]
        val card2 = flippedCards[1]

        if (card1.tag == card2.tag) {
            pairsFound++
            score += 100
            scoreTextView.text = getString(R.string.score_text, score)

            animateMatch(card1)
            animateMatch(card2)

            card1.isClickable = false
            card2.isClickable = false
            flippedCards.clear()
            isChecking = false

            if (pairsFound == cardIcons.size / 2) {
                endGame(true)
            }
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                val index1 = imageViews.indexOf(card1)
                val index2 = imageViews.indexOf(card2)
                flipBack(card1, index1)
                flipBack(card2, index2)
            }, 500)
        }
    }

    private fun animateMatch(card: ImageView) {
        val animatorSet = AnimatorSet()
        val scaleX = ObjectAnimator.ofFloat(card, "scaleX", 1f, 1.1f, 1f)
        val scaleY = ObjectAnimator.ofFloat(card, "scaleY", 1f, 1.1f, 1f)

        val peakElevationInDp = 24f
        val peakElevationInPixels = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, peakElevationInDp, resources.displayMetrics
        )

        val elevationAnimator = ObjectAnimator.ofFloat(card, "elevation", card.elevation, peakElevationInPixels, card.elevation)

        animatorSet.playTogether(scaleX, scaleY, elevationAnimator)
        animatorSet.duration = 600
        animatorSet.start()
    }

    private fun flipBack(imageView: ImageView, index: Int) {
        isFlipped[index] = false

        val oa1 = ObjectAnimator.ofFloat(imageView, "rotationY", 0f, 90f)
        val oa2 = ObjectAnimator.ofFloat(imageView, "rotationY", -90f, 0f)

        oa1.duration = 150
        oa2.duration = 150

        oa1.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                imageView.setBackgroundResource(cardBack)
                imageView.setImageDrawable(null)
                oa2.start()
            }
        })

        oa2.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                if(flippedCards.size == 2) {
                    flippedCards.clear()
                    isChecking = false
                }
            }
        })

        oa1.start()
    }

    private fun endGame(allPairsFound: Boolean) {
        countDownTimer.cancel()

        val timeBonus = if (allPairsFound) timeLeft * 5 else 0
        val difficultyMultiplier = when (level) {
            "easy" -> 1.0
            "medium" -> 1.5
            "hard" -> 2.0
            else -> 1.0
        }

        val finalScore = ((score + timeBonus) * difficultyMultiplier).toInt()

        val message = if (allPairsFound) {
            getString(R.string.congratulations_message, score, timeBonus.toInt(), finalScore)
        } else {
            getString(R.string.time_up_message, finalScore)
        }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.game_over_title))
            .setMessage(message)
            .setPositiveButton(getString(R.string.play_again)) { _, _ ->
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("level", level)
                startActivity(intent)
                finish()
            }
            .setNegativeButton(getString(R.string.home)) { _, _ ->
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }
            .setCancelable(false)
            .show()
    }
}