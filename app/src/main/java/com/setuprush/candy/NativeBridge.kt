package com.setuprush.candy

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.webkit.JavascriptInterface

/**
 * Native bridge interfacing the Setup Candy game engine with Android hardware features.
 * Provides micro-haptic feedback tailored for high-speed trading game interactions.
 */
class NativeBridge(private val context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    @JavascriptInterface
    fun vibrate(type: String) {
        if (vibrator == null || !vibrator.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = when (type.lowercase()) {
                    "tick" -> VibrationEffect.createOneShot(8, 40)
                    "hold_long" -> VibrationEffect.createWaveform(longArrayOf(0, 15, 25, 20), -1)
                    "slide_short" -> VibrationEffect.createWaveform(longArrayOf(0, 25, 20, 15), -1)
                    "cash_out", "win" -> VibrationEffect.createWaveform(longArrayOf(0, 15, 20, 30, 20, 45), -1)
                    "liquidation", "loss" -> VibrationEffect.createWaveform(longArrayOf(0, 50, 40, 100), -1)
                    else -> VibrationEffect.createOneShot(12, VibrationEffect.DEFAULT_AMPLITUDE)
                }
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                val millis = when (type.lowercase()) {
                    "tick" -> 10L
                    "liquidation", "loss" -> 120L
                    "cash_out" -> 40L
                    else -> 20L
                }
                vibrator.vibrate(millis)
            }

            // Sync with procedural synthesized audio
            when (type.lowercase()) {
                "tick" -> com.setuprush.candy.engine.SoundEffectManager.playTick()
                "cash_out" -> com.setuprush.candy.engine.SoundEffectManager.playCashout()
                "win" -> com.setuprush.candy.engine.SoundEffectManager.playVictory()
                "liquidation", "loss" -> com.setuprush.candy.engine.SoundEffectManager.playStopLoss()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Haptic error", e)
        }
    }

    @JavascriptInterface
    fun getPreferredTheme(): String = com.setuprush.candy.theme.ThemeManager.active().id

    @JavascriptInterface
    fun onThemeChanged(themeId: String) {
        Log.d(TAG, "Theme changed: $themeId")
        com.setuprush.candy.theme.ThemeManager.setTheme(themeId)
    }

    @JavascriptInterface
    fun getActiveTheme(): String = com.setuprush.candy.theme.ThemeManager.active().id

    @JavascriptInterface
    fun logEvent(event: String, payload: String?) {
        Log.d(TAG, "[$event] ${payload ?: ""}")
    }

    @JavascriptInterface
    fun launchNativeMode() {
        val intent = Intent(context, NativeGameActivity::class.java)
        context.startActivity(intent)
    }

    @JavascriptInterface
    fun isNativeApp(): Boolean = true

    companion object {
        private const val TAG = "SetupCandyBridge"
    }
}
