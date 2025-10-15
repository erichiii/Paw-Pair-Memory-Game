package com.example.paw_pair_memory_game

import android.content.Context
import android.media.SoundPool
import android.media.AudioAttributes

object SoundPlayer {
    private var soundPool: SoundPool? = null
    private var cardClickSoundId: Int = 0

    fun initialize(context: Context) {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        cardClickSoundId = soundPool?.load(context, R.raw.card_click, 1) ?: 0
    }

    fun playCardClick() {
        soundPool?.play(cardClickSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
    }

    fun release() {
        soundPool?.release()
        soundPool = null
    }
}
