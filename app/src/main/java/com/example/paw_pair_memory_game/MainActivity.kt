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
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.gridlayout.widget.GridLayout

class MainActivity : AppCompatActivity() {

    private lateinit var imageViews: MutableList<ImageView>
    private lateinit var cardIcons: List<Int>
    private lateinit var isFlipped: BooleanArray
    private val cardBack = R.drawable.card_back_1
    private val cardFront = R.drawable.card_front_2

    private val flippedCards = mutableListOf<ImageView>()
    private var isChecking = false
    private var isAnimating = false

    private lateinit var timerTextView: TextView
    private lateinit var timerProgressBar: ProgressBar
    private lateinit var scoreTextView: TextView

    private var score = 0
    private var pairsFound = 0
    private var timeLeft = 0L
    private lateinit var countDownTimer: CountDownTimer
    private lateinit var level: String

    private var comboCount = 0
    private lateinit var comboTextView: TextView
    private var baseScore = 0

    private var isPaused = false
    private lateinit var pauseButton: ImageButton
    private lateinit var gridLayout: GridLayout
    private var pauseDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        hideSystemUI()

        level = intent.getStringExtra("level") ?: "easy"
        gridLayout = findViewById(R.id.gridLayout)
        timerTextView = findViewById(R.id.timer_textview)
        timerProgressBar = findViewById(R.id.timer_progress_bar)
        scoreTextView = findViewById(R.id.score_textview)
        comboTextView = findViewById(R.id.combo_textview)
        scoreTextView.text = score.toString()

        SoundPlayer.initialize(this)
        MusicManager.fadeTo(0.3f, 500)

        onBackPressedDispatcher.addCallback(this) {
            if (!isPaused) {
                pauseGame()
            }
        }

        pauseButton = findViewById(R.id.pause_button)
        pauseButton.setOnClickListener {
            if (isPaused) {
                resumeGame()
            } else {
                pauseGame()
            }
        }

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
                if (isPaused || isAnimating) return@setOnClickListener
                if (!isChecking && !isFlipped[i] && flippedCards.size < 2) {
                    flipCard(imageView, i)
                    if (flippedCards.size == 2) {
                        isChecking = true
                    }
                }
            }
        }
    }

    private fun hideSystemUI() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    private fun pauseGame() {
        isPaused = true
        MusicManager.pause()
        countDownTimer.cancel()
        gridLayout.alpha = 0.5f
        pauseButton.setImageResource(android.R.drawable.ic_media_play)

        pauseDialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.game_paused_title))
            .setPositiveButton(getString(R.string.retry)) { _, _ ->
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
            .setOnCancelListener {
                resumeGame()
            }
            .show()
    }

    private fun resumeGame() {
        pauseDialog?.dismiss()
        isPaused = false
        MusicManager.resume()
        MusicManager.fadeTo(0.3f, 500)
        gridLayout.alpha = 1.0f
        startTimer(timeLeft * 1000)
        pauseButton.setImageResource(android.R.drawable.ic_media_pause)
        hideSystemUI()
    }

    override fun onPause() {
        super.onPause()
        if (!isPaused && !isChangingConfigurations) {
            MusicManager.pause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isPaused) {
            MusicManager.resume()
            MusicManager.fadeTo(0.3f, 500)
        }
        hideSystemUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        SoundPlayer.release()
    }

    private fun startTimer(timeLimit: Long) {
        timerProgressBar.max = (timeLimit / 1000).toInt()
        countDownTimer = object : CountDownTimer(timeLimit, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeft = millisUntilFinished / 1000
                timerTextView.text = timeLeft.toInt().toString()
                timerProgressBar.progress = timeLeft.toInt()
            }

            override fun onFinish() {
                timerTextView.text = "0"
                timerProgressBar.progress = 0
                endGame(false)
            }
        }.start()
    }

    private fun flipCard(imageView: ImageView, index: Int) {
        SoundPlayer.playCardClick()
        isAnimating = true
        isFlipped[index] = true
        flippedCards.add(imageView)

        val oa1 = ObjectAnimator.ofFloat(imageView, "rotationY", 0f, 90f)
        val oa2 = ObjectAnimator.ofFloat(imageView, "rotationY", -90f, 0f)

        oa1.duration = 50
        oa2.duration = 50

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
                isAnimating = false
                if (flippedCards.size == 2) {
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
            comboCount++

            val comboMultiplier = 1 + (comboCount / 2)
            val points = 100 * comboMultiplier
            score += points
            baseScore += 100


            scoreTextView.text = score.toString()
            if (comboCount > 1) {
                comboTextView.text = getString(R.string.combo_text, comboCount)
                comboTextView.visibility = View.VISIBLE
            }



            animateMatch(card1)
            animateMatch(card2)

            card1.isClickable = false
            card2.isClickable = false
            Handler(Looper.getMainLooper()).postDelayed({
                flippedCards.clear()
                isChecking = false
            }, 300)

            if (pairsFound == cardIcons.size / 2) {
                endGame(true)
            }
        } else {
            comboCount = 0
            comboTextView.visibility = View.GONE
            Handler(Looper.getMainLooper()).postDelayed({
                var flippedBackCount = 0
                val onFlippedBack = {
                    flippedBackCount++
                    if (flippedBackCount == 2) {
                        flippedCards.clear()
                        isChecking = false
                        isAnimating = false
                    }
                }

                val index1 = imageViews.indexOf(card1)
                val index2 = imageViews.indexOf(card2)
                isAnimating = true
                flipBack(card1, index1, onFlippedBack)
                flipBack(card2, index2, onFlippedBack)
            }, 600)
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
        animatorSet.duration = 300
        animatorSet.start()
    }

    private fun flipBack(imageView: ImageView, index: Int, onEnd: () -> Unit) {
        isFlipped[index] = false

        val oa1 = ObjectAnimator.ofFloat(imageView, "rotationY", 0f, 90f)
        val oa2 = ObjectAnimator.ofFloat(imageView, "rotationY", -90f, 0f)

        oa1.duration = 50
        oa2.duration = 50

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
                onEnd()
            }
        })

        oa1.start()
    }

    private fun endGame(allPairsFound: Boolean) {
        countDownTimer.cancel()
        pauseButton.isClickable = false
        MusicManager.fadeTo(1.0f, 1000)

        val timeBonus = if (allPairsFound) timeLeft * 5 else 0
        val difficultyMultiplier = when (level) {
            "easy" -> 1.0
            "medium" -> 1.5
            "hard" -> 2.0
            else -> 1.0
        }

        val comboBonus = score - baseScore
        val finalScore = ((baseScore + timeBonus + comboBonus) * difficultyMultiplier).toInt()

        val title: String
        val message: String

        if (allPairsFound) {
            title = getString(R.string.you_win_title)
            message = "Base Score: $baseScore\n" +
                    "Time Bonus: $timeBonus\n" +
                    "Combo Bonus: $comboBonus\n" +
                    "Difficulty Multiplier: x$difficultyMultiplier\n" +
                    "Final Score: $finalScore"
        } else {
            title = getString(R.string.game_over_title)
            message = getString(R.string.time_up_message, finalScore)
        }

        AlertDialog.Builder(this)
            .setTitle(title)
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
    }}