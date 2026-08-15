package com.example.engine

import com.example.data.model.AudioChunk
import com.example.data.model.Language
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

sealed interface TranscriptEvent {
    data class Partial(
        val originalText: String,
        val translatedText: String,
        val sourceLanguage: Language,
        val targetLanguage: Language
    ) : TranscriptEvent

    data class Final(
        val originalText: String,
        val translatedText: String,
        val sourceLanguage: Language,
        val targetLanguage: Language,
        val latencyMs: Long,
        val audioData: ByteArray? = null
    ) : TranscriptEvent

    data class Error(val message: String) : TranscriptEvent
}

interface TranslationEngine {
    val engineName: String
    val incomingAudioChunks: SharedFlow<ByteArray>
    val liveTranscriptEvents: StateFlow<TranscriptEvent?>

    suspend fun initializeSession(
        sourceLanguage: Language,
        targetLanguage: Language,
        voiceStylePreservation: Boolean = true,
        echoTargetLanguage: Boolean = false
    ): Boolean

    suspend fun startAudioConversation()
    fun stopAudioConversation()
    suspend fun streamAudio(chunk: AudioChunk)
    suspend fun finishUtterance(totalAudioRecorded: ByteArray? = null)

    suspend fun translateTextDirect(
        text: String,
        sourceLanguage: Language,
        targetLanguage: Language
    ): Pair<String, ByteArray?>

    suspend fun releaseSession()
}
