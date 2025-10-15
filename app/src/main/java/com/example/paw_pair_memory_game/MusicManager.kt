package com.example.paw_pair_memory_game

import android.content.Context
import android.media.MediaPlayer
import java.util.Timer
import java.util.TimerTask

object MusicManager {
    private var mediaPlayer: MediaPlayer? = null
    private var currentVolume = 1.0f
    private var fadeTimer: Timer? = null

    fun start(context: Context, loop: Boolean = true, volume: Float = 1.0f) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context.applicationContext, R.raw.bg_music)
            mediaPlayer?.isLooping = loop
        }
        mediaPlayer?.setVolume(0f, 0f)
        mediaPlayer?.start()
        fadeTo(volume, 1000)
    }

    fun pause() {
        mediaPlayer?.pause()
    }

    fun resume() {
        mediaPlayer?.start()
    }

    fun fadeTo(targetVolume: Float, duration: Long) {
        fadeTimer?.cancel()
        if (mediaPlayer?.isPlaying == true) {
            fadeTimer = Timer()
            val startTime = System.currentTimeMillis()
            val initialVolume = currentVolume

            val fadeTask = object : TimerTask() {
                override fun run() {
                    val elapsedTime = System.currentTimeMillis() - startTime
                    if (elapsedTime < duration) {
                        val fraction = elapsedTime.toFloat() / duration
                        val newVolume = initialVolume + (targetVolume - initialVolume) * fraction
                        mediaPlayer?.setVolume(newVolume, newVolume)
                        currentVolume = newVolume
                    } else {
                        mediaPlayer?.setVolume(targetVolume, targetVolume)
                        currentVolume = targetVolume
                        fadeTimer?.cancel()
                    }
                }
            }
            fadeTimer?.schedule(fadeTask, 0, 50)
        }
    }
}
