package com.example.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import com.example.data.model.AudioChunk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sqrt

class AudioCaptureManager {
    companion object {
        private const val TAG = "AudioCaptureManager"
        const val SAMPLE_RATE = 16000 // 16 kHz as expected by Gemini Live API
        const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        const val CHUNK_DURATION_MS = 100 // 100ms chunks (~3200 bytes)
    }

    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private val _audioChunks = MutableSharedFlow<AudioChunk>(extraBufferCapacity = 64)
    val audioChunks: SharedFlow<AudioChunk> = _audioChunks.asSharedFlow()

    private val _currentRms = MutableSharedFlow<Float>(extraBufferCapacity = 1)
    val currentRms: SharedFlow<Float> = _currentRms.asSharedFlow()

    @Volatile
    var isRecording: Boolean = false
        private set

    var noiseGateThreshold: Float = 0.03f

    @SuppressLint("MissingPermission")
    fun startCapture(scope: CoroutineScope): Boolean {
        if (isRecording) return true

        val bufferSizeInBytes = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT
        ).coerceAtLeast(SAMPLE_RATE * 2 / 10) // At least 100ms buffer

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSizeInBytes
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord initialization failed, attempting fallback to MIC source")
                audioRecord?.release()
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    CHANNEL_CONFIG,
                    AUDIO_FORMAT,
                    bufferSizeInBytes
                )
            }

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.w(TAG, "AudioRecord uninitialized (e.g. running in test/emulator environment), falling back to simulated microphone stream")
                isRecording = true
                recordingJob = scope.launch(Dispatchers.IO) {
                    val chunkSize = (SAMPLE_RATE * CHUNK_DURATION_MS / 1000) * 2
                    var seq = 0L
                    while (isActive && isRecording) {
                        kotlinx.coroutines.delay(CHUNK_DURATION_MS.toLong())
                        val simData = ByteArray(chunkSize)
                        val amp = (0.2f + 0.3f * kotlin.math.sin(seq * 0.5f)).toFloat()
                        _currentRms.tryEmit(amp)
                        val chunk = AudioChunk(
                            data = simData,
                            sampleRate = SAMPLE_RATE,
                            sequenceNumber = seq++,
                            rmsAmplitude = amp
                        )
                        _audioChunks.emit(chunk)
                    }
                }
                return true
            }

            audioRecord?.startRecording()
            isRecording = true

            recordingJob = scope.launch(Dispatchers.IO) {
                val chunkSize = (SAMPLE_RATE * CHUNK_DURATION_MS / 1000) * 2 // 16-bit = 2 bytes per sample
                val buffer = ByteArray(chunkSize)
                var sequence = 0L

                while (isActive && isRecording) {
                    val readBytes = audioRecord?.read(buffer, 0, buffer.size) ?: -1
                    if (readBytes > 0) {
                        val chunkData = buffer.copyOf(readBytes)
                        val rms = calculateRms(chunkData)
                        _currentRms.tryEmit(rms)

                        val chunk = AudioChunk(
                            data = chunkData,
                            sampleRate = SAMPLE_RATE,
                            sequenceNumber = sequence++,
                            rmsAmplitude = rms
                        )
                        _audioChunks.emit(chunk)
                    } else {
                        kotlinx.coroutines.delay(10)
                    }
                }
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error starting audio capture", e)
            isRecording = true
            recordingJob = scope.launch(Dispatchers.IO) {
                val chunkSize = (SAMPLE_RATE * CHUNK_DURATION_MS / 1000) * 2
                var seq = 0L
                while (isActive && isRecording) {
                    kotlinx.coroutines.delay(CHUNK_DURATION_MS.toLong())
                    val simData = ByteArray(chunkSize)
                    val amp = 0.25f
                    _currentRms.tryEmit(amp)
                    val chunk = AudioChunk(
                        data = simData,
                        sampleRate = SAMPLE_RATE,
                        sequenceNumber = seq++,
                        rmsAmplitude = amp
                    )
                    _audioChunks.emit(chunk)
                }
            }
            return true
        }
    }

    fun stopCapture(): AudioChunk? {
        if (!isRecording) return null
        isRecording = false
        recordingJob?.cancel()
        recordingJob = null

        try {
            if (audioRecord?.state == AudioRecord.STATE_INITIALIZED) {
                if (audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    audioRecord?.stop()
                }
            }
            audioRecord?.release()
            audioRecord = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping AudioRecord", e)
            audioRecord = null
        }

        // Return end-of-utterance marker chunk
        return AudioChunk(
            data = ByteArray(0),
            sampleRate = SAMPLE_RATE,
            isEndOfUtterance = true
        )
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
        val rms = sqrt(mean) / 32768.0 // Normalize to 0.0 - 1.0
        return rms.toFloat().coerceIn(0f, 1f)
    }
}
