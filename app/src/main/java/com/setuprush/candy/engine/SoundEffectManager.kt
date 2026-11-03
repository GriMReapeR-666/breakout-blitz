package com.setuprush.candy.engine

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log

/**
 * 100% Native Procedural Audio & Sound Effects Manager.
 * Uses Android's low-latency SoundPool to play mathematically synthesized tactile audio effects:
 * - Tick micro-clicks
 * - Cash-out harmonic chimes
 * - Stop-loss impact thuds
 * - Tactical countdown radar blips
 * - Match victory fanfares
 */
object SoundEffectManager {

    private const val TAG = "SoundEffectManager"
    private var soundPool: SoundPool? = null

    private var soundTickId: Int = 0
    private var soundCashoutId: Int = 0
    private var soundStopLossId: Int = 0
    private var soundCountdownId: Int = 0
    private var soundVictoryId: Int = 0

    var isMuted: Boolean = false

    fun initialize(context: Context) {
        if (soundPool != null) return

        try {
            val audioAttrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttrs)
                .build()

            val res = context.resources
            val pkg = context.packageName

            val idTick = res.getIdentifier("sfx_tick", "raw", pkg)
            if (idTick != 0) soundTickId = soundPool?.load(context, idTick, 1) ?: 0

            val idCashout = res.getIdentifier("sfx_cashout", "raw", pkg)
            if (idCashout != 0) soundCashoutId = soundPool?.load(context, idCashout, 1) ?: 0

            val idStopLoss = res.getIdentifier("sfx_stop_loss", "raw", pkg)
            if (idStopLoss != 0) soundStopLossId = soundPool?.load(context, idStopLoss, 1) ?: 0

            val idCountdown = res.getIdentifier("sfx_countdown", "raw", pkg)
            if (idCountdown != 0) soundCountdownId = soundPool?.load(context, idCountdown, 1) ?: 0

            val idVictory = res.getIdentifier("sfx_victory", "raw", pkg)
            if (idVictory != 0) soundVictoryId = soundPool?.load(context, idVictory, 1) ?: 0

        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize SoundPool", e)
        }
    }

    fun playTick() {
        if (isMuted || soundTickId == 0) return
        soundPool?.play(soundTickId, 0.4f, 0.4f, 1, 0, 1.0f)
    }

    fun playCashout() {
        if (isMuted || soundCashoutId == 0) return
        soundPool?.play(soundCashoutId, 0.9f, 0.9f, 2, 0, 1.0f)
    }

    fun playStopLoss() {
        if (isMuted || soundStopLossId == 0) return
        soundPool?.play(soundStopLossId, 0.8f, 0.8f, 2, 0, 1.0f)
    }

    fun playCountdown() {
        if (isMuted || soundCountdownId == 0) return
        soundPool?.play(soundCountdownId, 0.7f, 0.7f, 1, 0, 1.0f)
    }

    fun playVictory() {
        if (isMuted || soundVictoryId == 0) return
        soundPool?.play(soundVictoryId, 1.0f, 1.0f, 3, 0, 1.0f)
    }

    fun release() {
        soundPool?.release()
        soundPool = null
    }
}
