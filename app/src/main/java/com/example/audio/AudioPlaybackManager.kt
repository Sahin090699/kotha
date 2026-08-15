package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sqrt

class AudioPlaybackManager {
    companion object {
        private const val TAG = "AudioPlaybackManager"
        const val DEFAULT_SAMPLE_RATE = 24000 // 24 kHz for Gemini Live output
        const val CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_MONO
        const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null
    private val audioQueue = Channel<ByteArray>(Channel.UNLIMITED)

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackRms = MutableStateFlow(0f)
    val playbackRms: StateFlow<Float> = _playbackRms.asStateFlow()

    private var currentSampleRate = DEFAULT_SAMPLE_RATE

    fun initialize(sampleRate: Int = DEFAULT_SAMPLE_RATE) {
        currentSampleRate = sampleRate
        try {
            audioTrack?.release()
            audioTrack = null

            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                CHANNEL_CONFIG,
                AUDIO_FORMAT
            )

            val safeBufferSize = if (minBufferSize > 0) {
                minBufferSize.coerceAtLeast(sampleRate * 2 / 5)
            } else {
                sampleRate * 2 / 5
            }

            val newTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AUDIO_FORMAT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(CHANNEL_CONFIG)
                        .build()
                )
                .setBufferSizeInBytes(safeBufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            if (newTrack.state == AudioTrack.STATE_INITIALIZED) {
                try {
                    newTrack.setVolume(1.0f)
                    newTrack.play()
                } catch (e: Exception) {
                    Log.w(TAG, "AudioTrack initial play error", e)
                }
                audioTrack = newTrack
            } else {
                Log.w(TAG, "AudioTrack not initialized, fallback mode active")
                newTrack.release()
                audioTrack = null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing AudioTrack", e)
            audioTrack = null
        }
    }

    fun startPlaybackLoop(scope: CoroutineScope) {
        if (playbackJob != null && playbackJob?.isActive == true) return

        playbackJob = scope.launch(Dispatchers.IO) {
            try {
                while (isActive) {
                    val pcmData = audioQueue.receive()
                    if (pcmData.isEmpty()) {
                        _isPlaying.value = false
                        _playbackRms.value = 0f
                        continue
                    }

                    if (audioTrack == null || audioTrack?.state != AudioTrack.STATE_INITIALIZED) {
                        initialize(currentSampleRate)
                    }

                    _isPlaying.value = true
                    val rms = calculateRms(pcmData)
                    _playbackRms.value = rms

                    try {
                        if (audioTrack?.state == AudioTrack.STATE_INITIALIZED) {
                            if (audioTrack?.playState != AudioTrack.PLAYSTATE_PLAYING) {
                                audioTrack?.play()
                            }
                            audioTrack?.write(pcmData, 0, pcmData.size)
                        } else {
                            // If hardware AudioTrack is unavailable, simulate playback timing
                            val durationMs = (pcmData.size.toDouble() / (currentSampleRate * 2) * 1000).toLong()
                            kotlinx.coroutines.delay(durationMs.coerceIn(200, 2000))
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "AudioTrack write exception", e)
                    }

                    _isPlaying.value = false
                    _playbackRms.value = 0f
                }
            } catch (e: Exception) {
                Log.e(TAG, "Playback loop exception", e)
            } finally {
                _isPlaying.value = false
                _playbackRms.value = 0f
            }
        }
    }

    fun enqueueAudioChunk(pcmData: ByteArray) {
        if (pcmData.isNotEmpty()) {
            audioQueue.trySend(pcmData)
        }
    }

    fun clearQueueAndInterrupt() {
        // Drain the queue to immediately stop queued audio on user interruption
        while (audioQueue.tryReceive().isSuccess) {
            // drained
        }
        try {
            audioTrack?.pause()
            audioTrack?.flush()
        } catch (e: Exception) {
            Log.e(TAG, "Error flushing AudioTrack", e)
        }
        _isPlaying.value = false
        _playbackRms.value = 0f
    }

    fun release() {
        clearQueueAndInterrupt()
        playbackJob?.cancel()
        playbackJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing AudioTrack", e)
        }
    }

    private fun calculateRms(pcmData: ByteArray): Float {
        if (pcmData.isEmpty()) return 0f
        var sum = 0.0
        val sampleCount = pcmData.size / 2
        for (i in 0 until sampleCount) {
            val sample = (pcmData[i * 2].toInt() and 0xFF) or (pcmData[i * 2 + 1].toInt() shl 8)
            val shortVal = sample.toShort().toDouble()
            sum += shortVal * shortVal
        }
        val mean = sum / sampleCount
        val rms = sqrt(mean) / 32768.0
        return rms.toFloat().coerceIn(0f, 1f)
    }
}
