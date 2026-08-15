package com.example.data.model

data class AudioChunk(
    val data: ByteArray,
    val sampleRate: Int = 16000,
    val channels: Int = 1,
    val sequenceNumber: Long = 0,
    val isEndOfUtterance: Boolean = false,
    val rmsAmplitude: Float = 0f
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AudioChunk
        return data.contentEquals(other.data) && sequenceNumber == other.sequenceNumber
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + sequenceNumber.hashCode()
        return result
    }
}

data class AudioVisualizerState(
    val isLive: Boolean = false,
    val amplitude: Float = 0f,
    val waveformFrequencies: List<Float> = List(16) { 0.1f }
)
