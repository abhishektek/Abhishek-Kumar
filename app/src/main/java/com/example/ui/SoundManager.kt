package com.example.ui

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

object SoundManager {
    private val scope = CoroutineScope(Dispatchers.IO)
    private const val SAMPLE_RATE = 22050 // Optimized sample rate for clean and compact mobile audio synthesis

    /**
     * Synthesizes and plays a sequential wave tone pattern completely programmatically.
     * Uses ADSR (Attack, Decay, Sustain, Release) envelope smoothing to eliminate speaker static/popping.
     */
    fun playTone(
        frequencies: FloatArray,
        durationsMs: IntArray,
        type: String = "sine"
    ) {
        scope.launch {
            try {
                var totalSamples = 0
                for (d in durationsMs) {
                    totalSamples += (SAMPLE_RATE * d / 1000)
                }

                if (totalSamples == 0) return@launch
                val buffer = ShortArray(totalSamples)
                var currentSampleOffset = 0

                for (i in frequencies.indices) {
                    val freq = frequencies[i]
                    val durationMs = durationsMs[i]
                    val samplesForNote = (SAMPLE_RATE * durationMs / 1000)

                    for (s in 0 until samplesForNote) {
                        val t = s.toDouble() / SAMPLE_RATE
                        
                        // Generate waveforms
                        val rawValue = when (type) {
                            "square" -> if (sin(2.0 * Math.PI * freq * t) >= 0.0) 1.0 else -1.0
                            "triangle" -> {
                                val period = SAMPLE_RATE / freq
                                val phase = (s % period) / period
                                if (phase < 0.5) 4.0 * phase - 1.0 else 3.0 - 4.0 * phase
                            }
                            else -> sin(2.0 * Math.PI * freq * t) // pure sine wave
                        }

                        // Smooth transition boundaries (ADSR) to avoid audio clicks
                        val adsrMultiplier = when {
                            s < samplesForNote * 0.1 -> s / (samplesForNote * 0.1) // Attack
                            s > samplesForNote * 0.75 -> (samplesForNote - s) / (samplesForNote * 0.25) // Release
                            else -> 1.0 // Sustain
                        }

                        // Scale volume comfortable for game usage (15% max volume to be pleasant and sublte)
                        val sampleValue = (rawValue * 32767.0 * 0.15 * adsrMultiplier).toInt()
                        val coercedValue = sampleValue.coerceIn(-32768, 32767).toShort()
                        
                        if (currentSampleOffset + s < totalSamples) {
                            buffer[currentSampleOffset + s] = coercedValue
                        }
                    }
                    currentSampleOffset += samplesForNote
                }

                // Initialize static fast-transfer AudioTrack
                val minBufferSize = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                val bufferSizeInBytes = maxOf(buffer.size * 2, minBufferSize)

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
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSizeInBytes)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()
                
                // Keep playing until done, then release hardware stream
                val totalDurationMs = durationsMs.sum()
                delay(totalDurationMs.toLong() + 50L)
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Synthesized auditory sound effect for matching candies successfully.
     * Dynamic pitch scaling based on cascade combo multiplier.
     */
    fun playCandyMatch(combo: Int = 1) {
        val baseFreq = 523.25f // C5
        val multiplier = 1.0f + (combo - 1) * 0.12f // rises pleasantly for larger combos
        val f1 = baseFreq * multiplier
        val f2 = f1 * 1.25f // Major third chime
        
        playTone(
            frequencies = floatArrayOf(f1, f2),
            durationsMs = intArrayOf(50, 70),
            type = "sine"
        )
    }

    /**
     * Auditory response on point accumulation. Simple rising bubble arpeggio.
     */
    fun playPointAccumulation() {
        playTone(
            frequencies = floatArrayOf(587.33f, 659.25f, 783.99f, 880.00f), // D5, E5, G5, A5
            durationsMs = intArrayOf(40, 40, 40, 60),
            type = "sine"
        )
    }

    /**
     * Triumphant golden melody cascade on Level Win.
     */
    fun playLevelWin() {
        playTone(
            frequencies = floatArrayOf(523.25f, 659.25f, 783.99f, 1046.50f, 1318.51f, 1567.98f), // C5, E5, G5, C6, E6, G6
            durationsMs = intArrayOf(70, 70, 70, 70, 70, 200),
            type = "sine"
        )
    }

    /**
     * Satisfying electronic coin cash register chime on rewards redemption success!
     */
    fun playRedemptionSuccess() {
        playTone(
            frequencies = floatArrayOf(987.77f, 1318.51f, 1567.98f, 1975.53f), // B5 -> E6 -> G6 -> B6
            durationsMs = intArrayOf(60, 60, 60, 250),
            type = "triangle"
        )
    }

    /**
     * Gentle buzz to indicate feedback error/limit (e.g. redemption balance missing).
     */
    fun playRedemptionFailure() {
        playTone(
            frequencies = floatArrayOf(180f, 130f),
            durationsMs = intArrayOf(120, 150),
            type = "triangle"
        )
    }

    /**
     * Soft pop tone when daily rewards are earned.
     */
    fun playDailyBonusSuccess() {
        playTone(
            frequencies = floatArrayOf(440.00f, 554.37f, 659.25f, 880.00f), // A4 -> C#5 -> E5 -> A5
            durationsMs = intArrayOf(60, 60, 60, 180),
            type = "sine"
        )
    }
}
