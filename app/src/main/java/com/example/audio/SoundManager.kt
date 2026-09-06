package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class SoundManager(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    var isSoundEnabled: Boolean = true
    var isHapticsEnabled: Boolean = true

    fun playDiceRoll() {
        if (isHapticsEnabled) {
            vibrate(longArrayOf(0, 30, 40, 30, 40, 50), intArrayOf(0, 100, 0, 150, 0, 200))
        }
        if (!isSoundEnabled) return
        scope.launch {
            // Rapid clacking percussion sound
            for (i in 0 until 4) {
                playTone(freq = 400.0 + (i * 80), durationMs = 25, volume = 0.6f)
                kotlinx.coroutines.delay(35)
            }
            playTone(freq = 750.0, durationMs = 50, volume = 0.8f)
        }
    }

    fun playStepHop() {
        if (isHapticsEnabled) {
            vibrate(longArrayOf(0, 20), intArrayOf(0, 80))
        }
        if (!isSoundEnabled) return
        scope.launch {
            playTone(freq = 520.0, durationMs = 35, volume = 0.5f)
        }
    }

    fun playSafeTile() {
        if (isHapticsEnabled) {
            vibrate(longArrayOf(0, 30, 30, 40), intArrayOf(0, 120, 0, 180))
        }
        if (!isSoundEnabled) return
        scope.launch {
            playTone(freq = 880.0, durationMs = 70, volume = 0.7f)
            kotlinx.coroutines.delay(60)
            playTone(freq = 1174.66, durationMs = 120, volume = 0.8f)
        }
    }

    fun playBonusTurn() {
        if (isHapticsEnabled) {
            vibrate(longArrayOf(0, 40, 30, 60), intArrayOf(0, 150, 0, 220))
        }
        if (!isSoundEnabled) return
        scope.launch {
            playTone(freq = 659.25, durationMs = 60, volume = 0.7f)
            kotlinx.coroutines.delay(50)
            playTone(freq = 880.0, durationMs = 60, volume = 0.8f)
            kotlinx.coroutines.delay(50)
            playTone(freq = 1318.51, durationMs = 120, volume = 0.9f)
        }
    }

    fun playCapture() {
        if (isHapticsEnabled) {
            vibrate(longArrayOf(0, 80, 40, 120), intArrayOf(0, 200, 0, 255))
        }
        if (!isSoundEnabled) return
        scope.launch {
            playTone(freq = 300.0, durationMs = 60, volume = 0.9f)
            kotlinx.coroutines.delay(40)
            playTone(freq = 180.0, durationMs = 120, volume = 1.0f)
        }
    }

    fun playHomeGoal() {
        if (isHapticsEnabled) {
            vibrate(longArrayOf(0, 50, 40, 80), intArrayOf(0, 180, 0, 240))
        }
        if (!isSoundEnabled) return
        scope.launch {
            val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50)
            for (note in notes) {
                playTone(freq = note, durationMs = 70, volume = 0.8f)
                kotlinx.coroutines.delay(60)
            }
        }
    }

    fun playVictory() {
        if (isHapticsEnabled) {
            vibrate(longArrayOf(0, 100, 50, 100, 50, 200), intArrayOf(0, 200, 0, 220, 0, 255))
        }
        if (!isSoundEnabled) return
        scope.launch {
            val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50, 880.0, 1046.50, 1318.51)
            for (note in notes) {
                playTone(freq = note, durationMs = 110, volume = 0.9f)
                kotlinx.coroutines.delay(100)
            }
        }
    }

    fun playButtonClick() {
        if (isHapticsEnabled) {
            vibrate(longArrayOf(0, 15), intArrayOf(0, 60))
        }
        if (!isSoundEnabled) return
        scope.launch {
            playTone(freq = 700.0, durationMs = 20, volume = 0.4f)
        }
    }

    private fun playTone(freq: Double, durationMs: Int, volume: Float = 0.8f) {
        try {
            val sampleRate = 22050
            val numSamples = (durationMs * sampleRate) / 1000
            val samples = ShortArray(numSamples)
            val decay = numSamples.toDouble()

            for (i in 0 until numSamples) {
                val envelope = 1.0 - (i.toDouble() / decay) // linear decay
                val angle = 2.0 * PI * i / (sampleRate / freq)
                val sampleValue = (sin(angle) * envelope * volume * Short.MAX_VALUE).toInt()
                samples[i] = sampleValue.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }

            val bufferSize = numSamples * 2
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(samples, 0, numSamples)
            audioTrack.play()

            // Release after playing
            Thread.sleep(durationMs.toLong() + 20)
            audioTrack.stop()
            audioTrack.release()
        } catch (_: Exception) {
            // Ignore audio generation issues safely
        }
    }

    private fun vibrate(timings: LongArray, amplitudes: IntArray) {
        try {
            if (vibrator == null || !vibrator.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(timings.sum())
            }
        } catch (_: Exception) {
        }
    }
}
