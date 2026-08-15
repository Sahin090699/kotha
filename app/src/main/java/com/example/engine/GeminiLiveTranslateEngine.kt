package com.example.engine

import android.util.Log
import com.example.data.model.AudioChunk
import com.example.data.model.Language
import com.google.firebase.ai.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.type.AudioTranscriptionConfig
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.liveGenerationConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * Production translation engine backed by Firebase AI Logic / Gemini Live API.
 *
 * The previous implementation called the REST API directly, parsed JSON with
 * regexes, generated synthetic tones, and returned fabricated sample phrases
 * when the API failed. This implementation deliberately fails closed: an AI
 * failure becomes TranscriptEvent.Error instead of fake content.
 */
class GeminiLiveTranslateEngine(
    private val scope: CoroutineScope
) : TranslationEngine {

    companion object {
        private const val TAG = "GeminiLiveTranslate"
        private const val LIVE_MODEL = "gemini-2.5-flash-native-audio-preview-12-2025"
        private const val TEXT_MODEL = "gemini-2.5-flash"
    }

    override val engineName: String = "Gemini Live Translator"

    private val _incomingAudioChunks = MutableSharedFlow<ByteArray>(extraBufferCapacity = 64)
    override val incomingAudioChunks: SharedFlow<ByteArray> = _incomingAudioChunks.asSharedFlow()

    private val _liveTranscriptEvents = MutableStateFlow<TranscriptEvent?>(null)
    override val liveTranscriptEvents: StateFlow<TranscriptEvent?> = _liveTranscriptEvents.asStateFlow()

    private var sourceLanguage = Language.Bengali
    private var targetLanguage = Language.English
    private var liveSession: com.google.firebase.ai.type.LiveSession? = null
    private var liveStarted = false
    private var turnStartedAt = 0L
    private var lastInputTranscript = ""

    override suspend fun initializeSession(
        sourceLanguage: Language,
        targetLanguage: Language,
        voiceStylePreservation: Boolean,
        echoTargetLanguage: Boolean
    ): Boolean = withContext(Dispatchers.Default) {
        this@GeminiLiveTranslateEngine.sourceLanguage = sourceLanguage
        this@GeminiLiveTranslateEngine.targetLanguage = targetLanguage
        _liveTranscriptEvents.value = null
        lastInputTranscript = ""
        turnStartedAt = 0L

        // A new LiveModel is created for every language pair because the system
        // instruction is part of model construction in Firebase AI Logic.
        closeLiveSession()
        true
    }

    private fun createLiveModel() = Firebase.ai(
        backend = GenerativeBackend.googleAI()
    ).liveModel(
        modelName = LIVE_MODEL,
        systemInstruction = content {
            text(
                """
                You are Kotha, a professional real-time speech interpreter.
                Translate naturally between ${sourceLanguage.englishName} and ${targetLanguage.englishName}.
                The speaker may code-switch between Bengali and English.
                Preserve meaning, names, numbers, tone and conversational intent.
                Do not answer questions or add commentary; interpret what the speaker says.
                Respond in the target language only.
                """.trimIndent()
            )
        },
        generationConfig = liveGenerationConfig {
            responseModality = ResponseModality.AUDIO
            inputAudioTranscription = AudioTranscriptionConfig()
            outputAudioTranscription = AudioTranscriptionConfig()
        }
    )

    override suspend fun startAudioConversation() {
        try {
            if (liveStarted && liveSession?.isClosed == false) return

            val session = createLiveModel().connect()
            liveSession = session
            turnStartedAt = System.currentTimeMillis()

            session.startAudioConversation(
                functionCallHandler = null,
                transcriptHandler = { input, output ->
                    val inputText = input?.text.orEmpty().trim()
                    val outputText = output?.text.orEmpty().trim()

                    if (inputText.isNotEmpty()) {
                        lastInputTranscript = inputText
                        _liveTranscriptEvents.value = TranscriptEvent.Partial(
                            originalText = inputText,
                            translatedText = "Translating…",
                            sourceLanguage = sourceLanguage,
                            targetLanguage = targetLanguage
                        )
                    }

                    if (outputText.isNotEmpty()) {
                        val latency = if (turnStartedAt > 0) {
                            System.currentTimeMillis() - turnStartedAt
                        } else 0L

                        _liveTranscriptEvents.value = TranscriptEvent.Final(
                            originalText = lastInputTranscript,
                            translatedText = outputText,
                            sourceLanguage = sourceLanguage,
                            targetLanguage = targetLanguage,
                            latencyMs = latency,
                            audioData = null
                        )
                        lastInputTranscript = ""
                        turnStartedAt = System.currentTimeMillis()
                    }
                },
                enableInterruptions = true
            )
            liveStarted = true
        } catch (e: Exception) {
            liveStarted = false
            liveSession = null
            Log.e(TAG, "Unable to start Gemini Live session", e)
            _liveTranscriptEvents.value = TranscriptEvent.Error(
                e.message ?: "Unable to start live translation. Check network, Firebase AI configuration, and App Check."
            )
        }
    }

    override fun stopAudioConversation() {
        try {
            liveSession?.stopAudioConversation()
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping live audio conversation", e)
        } finally {
            liveStarted = false
        }
    }

    override suspend fun streamAudio(chunk: AudioChunk) {
        // Audio capture is owned by Firebase AI Logic while a live audio
        // conversation is active. Keeping this method for the engine contract
        // avoids a second competing microphone pipeline.
    }

    override suspend fun finishUtterance(totalAudioRecorded: ByteArray?) {
        // Gemini Live uses server-side VAD and turn completion. There is no
        // fabricated end marker or local transcription fallback.
    }

    override suspend fun translateTextDirect(
        text: String,
        sourceLanguage: Language,
        targetLanguage: Language
    ): Pair<String, ByteArray?> = withContext(Dispatchers.IO) {
        try {
            val model: GenerativeModel = Firebase.ai(
                backend = GenerativeBackend.googleAI()
            ).generativeModel(
                modelName = TEXT_MODEL,
                systemInstruction = content {
                    text(
                        "Translate accurately from ${sourceLanguage.englishName} to ${targetLanguage.englishName}. " +
                            "Preserve meaning, names, numbers and Bengali-English code-switching. Output only the translation."
                    )
                }
            )
            val response = model.generateContent(text)
            val translated = response.text?.trim().orEmpty()
            if (translated.isBlank()) {
                throw IllegalStateException("Translation service returned an empty response")
            }
            translated to null
        } catch (e: Exception) {
            Log.e(TAG, "Direct translation failed", e)
            throw e
        }
    }

    override suspend fun releaseSession() {
        closeLiveSession()
        _liveTranscriptEvents.value = null
        lastInputTranscript = ""
    }

    private fun closeLiveSession() {
        try {
            liveSession?.close()
        } catch (e: Exception) {
            Log.w(TAG, "Error closing Gemini Live session", e)
        } finally {
            liveSession = null
            liveStarted = false
        }
    }
}
