package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioCaptureManager
import com.example.audio.AudioPlaybackManager
import com.example.audio.AudioRouter
import com.example.data.local.AppDatabase
import com.example.data.local.HistoryRepository
import com.example.data.local.SessionHistoryEntity
import com.example.data.local.TranscriptEntity
import com.example.data.model.AudioActivity
import com.example.data.model.AudioVisualizerState
import com.example.data.model.ConnectionStatus
import com.example.data.model.Language
import com.example.data.model.SessionConfig
import com.example.data.model.TranscriptMessage
import com.example.engine.GeminiLiveTranslateEngine
import com.example.engine.TranscriptEvent
import com.example.engine.TranslationEngine
import com.example.transport.PairingInfo
import com.example.transport.PairingManager
import com.example.transport.RelayTransport
import com.example.transport.TransportPacket
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppScreen {
    HOME,
    HOST_PAIRING,
    JOIN_PAIRING,
    ACTIVE_TRANSLATE,
    HISTORY,
    SETTINGS
}

class KothaViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    val historyRepository = HistoryRepository(database.sessionDao())

    val audioCaptureManager = AudioCaptureManager()
    val audioPlaybackManager = AudioPlaybackManager()
    val audioRouter = AudioRouter(application)
    val relayTransport = RelayTransport(viewModelScope)
    val translationEngine: TranslationEngine = GeminiLiveTranslateEngine(viewModelScope)

    // UI States
    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _sessionConfig = MutableStateFlow(
        SessionConfig(
            userName = "Rahim",
            spokenLanguage = Language.Bengali,
            heardLanguage = Language.English,
            isPushToTalk = true,
            noiseGateThreshold = 0.03f,
            audioRoutingSpeaker = true
        )
    )
    val sessionConfig: StateFlow<SessionConfig> = _sessionConfig.asStateFlow()

    private val _pairingInfo = MutableStateFlow<PairingInfo?>(null)
    val pairingInfo: StateFlow<PairingInfo?> = _pairingInfo.asStateFlow()

    val connectionStatus: StateFlow<ConnectionStatus> = relayTransport.connectionStatus

    private val _audioActivity = MutableStateFlow(AudioActivity.IDLE)
    val audioActivity: StateFlow<AudioActivity> = _audioActivity.asStateFlow()

    private val _transcripts = MutableStateFlow<List<TranscriptMessage>>(emptyList())
    val transcripts: StateFlow<List<TranscriptMessage>> = _transcripts.asStateFlow()

    private val _visualizerState = MutableStateFlow(AudioVisualizerState())
    val visualizerState: StateFlow<AudioVisualizerState> = _visualizerState.asStateFlow()

    private val _hasMicConsent = MutableStateFlow(true)
    val hasMicConsent: StateFlow<Boolean> = _hasMicConsent.asStateFlow()

    private val _showPrivacyDialog = MutableStateFlow(false)
    val showPrivacyDialog: StateFlow<Boolean> = _showPrivacyDialog.asStateFlow()

    val recentSessions = historyRepository.allSessions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private var currentSessionId: String = UUID.randomUUID().toString()
    private var sessionStartTime: Long = 0
    private var captureChunkCollectorJob: Job? = null
    private var visualizerRmsJob: Job? = null

    init {
        // Initialize audio playback loop
        audioPlaybackManager.initialize(24000)
        audioPlaybackManager.startPlaybackLoop(viewModelScope)

        // Observe incoming packets from Relay Transport (Peer's speech and transcripts)
        viewModelScope.launch {
            relayTransport.incomingPackets.collect { packet ->
                when (packet) {
                    is TransportPacket.AudioPayload -> {
                        _audioActivity.value = AudioActivity.PLAYING_PEER_AUDIO
                        audioPlaybackManager.enqueueAudioChunk(packet.audioData)
                    }
                    is TransportPacket.TranscriptPayload -> {
                        addTranscriptMessage(packet.message)
                    }
                    is TransportPacket.EndSession -> {
                        _audioActivity.value = AudioActivity.IDLE
                    }
                    else -> Unit
                }
            }
        }

        // Observe playback state to return to idle once peer finishes speaking
        viewModelScope.launch {
            audioPlaybackManager.isPlaying.collect { isPlaying ->
                if (!isPlaying && _audioActivity.value == AudioActivity.PLAYING_PEER_AUDIO) {
                    _audioActivity.value = AudioActivity.IDLE
                }
            }
        }

        // Observe translation engine live events
        viewModelScope.launch {
            translationEngine.liveTranscriptEvents.collect { event ->
                when (event) {
                    is TranscriptEvent.Final -> {
                        val msg = TranscriptMessage(
                            speakerId = "local_user",
                            speakerName = _sessionConfig.value.userName,
                            isLocalUser = true,
                            originalText = event.originalText,
                            translatedText = event.translatedText,
                            sourceLanguage = event.sourceLanguage,
                            targetLanguage = event.targetLanguage,
                            latencyMs = event.latencyMs
                        )
                        addTranscriptMessage(msg)
                        // Send to peer over relay transport
                        relayTransport.sendTranscriptToPeer(msg)
                        if (event.audioData != null) {
                            relayTransport.sendAudioToPeer(event.audioData)
                        }
                        _audioActivity.value = AudioActivity.IDLE
                    }
                    is TranscriptEvent.Partial -> {
                        _audioActivity.value = AudioActivity.TRANSLATING
                    }
                    else -> Unit
                }
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun setPrivacyConsent(granted: Boolean) {
        _hasMicConsent.value = granted
        _showPrivacyDialog.value = false
    }

    fun showPrivacyConsent() {
        _showPrivacyDialog.value = true
    }

    fun updateConfig(config: SessionConfig) {
        _sessionConfig.value = config
        audioRouter.setSpeakerphoneOn(config.audioRoutingSpeaker)
    }

    fun startDirectLiveSession() {
        currentSessionId = UUID.randomUUID().toString()
        sessionStartTime = System.currentTimeMillis()
        _transcripts.value = emptyList()

        relayTransport.joinSession(
            code = "SOLO_LIVE",
            userName = _sessionConfig.value.userName,
            userLanguage = _sessionConfig.value.spokenLanguage,
            isSimulation = true
        )

        viewModelScope.launch {
            translationEngine.initializeSession(
                sourceLanguage = _sessionConfig.value.spokenLanguage,
                targetLanguage = _sessionConfig.value.heardLanguage,
                voiceStylePreservation = _sessionConfig.value.voicePitchTransferEnabled,
                echoTargetLanguage = _sessionConfig.value.echoTargetLanguage
            )
            _currentScreen.value = AppScreen.ACTIVE_TRANSLATE
        }
    }

    fun startHostSession() {
        currentSessionId = UUID.randomUUID().toString()
        sessionStartTime = System.currentTimeMillis()
        _transcripts.value = emptyList()

        val pairing = relayTransport.startHostSession(
            hostName = _sessionConfig.value.userName,
            hostLanguage = _sessionConfig.value.spokenLanguage
        )
        _pairingInfo.value = pairing
        _currentScreen.value = AppScreen.HOST_PAIRING

        // Initialize translation engine
        viewModelScope.launch {
            translationEngine.initializeSession(
                sourceLanguage = _sessionConfig.value.spokenLanguage,
                targetLanguage = _sessionConfig.value.heardLanguage,
                voiceStylePreservation = _sessionConfig.value.voicePitchTransferEnabled,
                echoTargetLanguage = _sessionConfig.value.echoTargetLanguage
            )
        }
    }

    fun joinWithCode(code: String, isSimulation: Boolean = false) {
        currentSessionId = UUID.randomUUID().toString()
        sessionStartTime = System.currentTimeMillis()
        _transcripts.value = emptyList()

        relayTransport.joinSession(
            code = code,
            userName = _sessionConfig.value.userName,
            userLanguage = _sessionConfig.value.spokenLanguage,
            isSimulation = isSimulation
        )

        viewModelScope.launch {
            translationEngine.initializeSession(
                sourceLanguage = _sessionConfig.value.spokenLanguage,
                targetLanguage = _sessionConfig.value.heardLanguage,
                voiceStylePreservation = _sessionConfig.value.voicePitchTransferEnabled,
                echoTargetLanguage = _sessionConfig.value.echoTargetLanguage
            )
            // Wait for connection to transition to active screen
            delay(900)
            _currentScreen.value = AppScreen.ACTIVE_TRANSLATE
        }
    }

    fun simulatePeerJoinInHost() {
        val code = _pairingInfo.value?.sessionCode ?: "KT7B29"
        relayTransport.simulateHostPeerJoined(code, _sessionConfig.value.heardLanguage)
        _currentScreen.value = AppScreen.ACTIVE_TRANSLATE
    }

    fun startPushToTalk() {
        // Interrupt any playing audio immediately
        audioPlaybackManager.clearQueueAndInterrupt()

        _audioActivity.value = AudioActivity.CAPTURING_LOCAL
        val started = audioCaptureManager.startCapture(viewModelScope)
        if (!started) {
            _audioActivity.value = AudioActivity.IDLE
            return
        }

        // Stream captured chunks to engine
        captureChunkCollectorJob?.cancel()
        captureChunkCollectorJob = viewModelScope.launch {
            audioCaptureManager.audioChunks.collect { chunk ->
                translationEngine.streamAudio(chunk)
            }
        }

        // Update visualizer
        visualizerRmsJob?.cancel()
        visualizerRmsJob = viewModelScope.launch {
            audioCaptureManager.currentRms.collect { rms ->
                val bars = List(16) { i ->
                    val factor = 0.3f + 0.7f * kotlin.math.abs(kotlin.math.sin(i * 0.45f + System.currentTimeMillis() * 0.01f)).toFloat()
                    (rms * factor * 1.8f).coerceIn(0.08f, 1.0f)
                }
                _visualizerState.value = AudioVisualizerState(
                    isLive = true,
                    amplitude = rms,
                    waveformFrequencies = bars
                )
            }
        }
    }

    fun stopPushToTalk() {
        _audioActivity.value = AudioActivity.TRANSLATING
        val endMarker = audioCaptureManager.stopCapture()
        captureChunkCollectorJob?.cancel()
        captureChunkCollectorJob = null
        visualizerRmsJob?.cancel()
        visualizerRmsJob = null

        _visualizerState.value = AudioVisualizerState(isLive = false, amplitude = 0f)

        viewModelScope.launch {
            translationEngine.finishUtterance()
        }
    }

    fun injectTestUtterance(bengaliText: String) {
        viewModelScope.launch {
            _audioActivity.value = AudioActivity.TRANSLATING
            val result = translationEngine.translateTextDirect(
                text = bengaliText,
                sourceLanguage = _sessionConfig.value.spokenLanguage,
                targetLanguage = _sessionConfig.value.heardLanguage
            )

            val msg = TranscriptMessage(
                speakerId = "local_user",
                speakerName = _sessionConfig.value.userName,
                isLocalUser = true,
                originalText = bengaliText,
                translatedText = result.first,
                sourceLanguage = _sessionConfig.value.spokenLanguage,
                targetLanguage = _sessionConfig.value.heardLanguage,
                latencyMs = 320
            )
            addTranscriptMessage(msg)
            relayTransport.sendTranscriptToPeer(msg)

            if (result.second != null) {
                audioPlaybackManager.enqueueAudioChunk(result.second!!)
            }
            _audioActivity.value = AudioActivity.IDLE
        }
    }

    fun triggerSimulatedPeerTurn() {
        val peerResponses = listOf(
            Pair("ভাই, meeting টা কি postpone হয়ে গেছে?", "Brother, has the meeting been postponed?"),
            Pair("Yes, the client rescheduled to 4 PM.", "হ্যাঁ, ক্লায়েন্ট মিটিং ৪ টায় পুনর্নির্ধারণ করেছেন।"),
            Pair("আমি airport-এ পৌঁছে গেছি, luggage collect করছি।", "I have arrived at the airport, collecting luggage."),
            Pair("Great! Take a taxi to the hotel, I'll meet you at the lobby.", "দারুণ! হোটেলে ট্যাক্সি নিন, লবিতে দেখা হবে।"),
            Pair("আজকের lunch এ কি অর্ডার করব?", "What should we order for today's lunch?"),
            Pair("Let's order some spicy biryani!", "চলুন কিছুটা স্পাইসি বিরিয়ানি অর্ডার করি!")
        )

        val idx = (_transcripts.value.size % peerResponses.size)
        val selected = peerResponses[idx]
        val isPeerBengali = _sessionConfig.value.heardLanguage.iso639_1 == "bn"

        val peerOriginal = if (isPeerBengali) selected.first else selected.second
        val peerTranslated = if (isPeerBengali) selected.second else selected.first
        val peerSourceLang = if (isPeerBengali) Language.Bengali else Language.English
        val peerTargetLang = if (isPeerBengali) Language.English else Language.Bengali

        viewModelScope.launch {
            val result = translationEngine.translateTextDirect(
                text = peerOriginal,
                sourceLanguage = peerSourceLang,
                targetLanguage = peerTargetLang
            )
            relayTransport.triggerPeerResponse(
                peerBengaliText = peerOriginal,
                translatedEnglishText = result.first,
                sourceLang = peerSourceLang,
                targetLang = peerTargetLang,
                audioPcm = result.second
            )
        }
    }

    fun toggleAudioRouting() {
        val newRoute = !_sessionConfig.value.audioRoutingSpeaker
        _sessionConfig.value = _sessionConfig.value.copy(audioRoutingSpeaker = newRoute)
        audioRouter.setSpeakerphoneOn(newRoute)
    }

    fun endSession() {
        val endTime = System.currentTimeMillis()
        val totalMsgs = _transcripts.value.size
        val peerName = when (val s = connectionStatus.value) {
            is ConnectionStatus.Connected -> s.peerName
            else -> "Participant"
        }

        // Save session to Room DB
        viewModelScope.launch {
            val sessionEntity = SessionHistoryEntity(
                sessionId = currentSessionId,
                peerName = peerName,
                sourceLanguageCode = _sessionConfig.value.spokenLanguage.iso639_1,
                targetLanguageCode = _sessionConfig.value.heardLanguage.iso639_1,
                startTimeMillis = if (sessionStartTime > 0) sessionStartTime else endTime - 120000,
                endTimeMillis = endTime,
                totalUtterances = totalMsgs,
                summary = _transcripts.value.lastOrNull()?.translatedText ?: "Conversation finished"
            )
            historyRepository.saveSession(sessionEntity)
        }

        relayTransport.disconnect("User ended call")
        audioCaptureManager.stopCapture()
        audioPlaybackManager.clearQueueAndInterrupt()
        _audioActivity.value = AudioActivity.IDLE
        _currentScreen.value = AppScreen.HOME
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            historyRepository.deleteSession(sessionId)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            historyRepository.clearAllHistory()
        }
    }

    fun replayMessageAudio(message: TranscriptMessage) {
        viewModelScope.launch {
            val result = translationEngine.translateTextDirect(
                text = message.translatedText,
                sourceLanguage = message.sourceLanguage,
                targetLanguage = message.targetLanguage
            )
            if (result.second != null) {
                audioPlaybackManager.clearQueueAndInterrupt()
                audioPlaybackManager.enqueueAudioChunk(result.second!!)
            }
        }
    }

    private fun addTranscriptMessage(message: TranscriptMessage) {
        _transcripts.value = _transcripts.value + message
        // Save to Room DB
        viewModelScope.launch {
            val entity = TranscriptEntity(
                id = message.id,
                sessionId = currentSessionId,
                speakerName = message.speakerName,
                isLocalUser = message.isLocalUser,
                originalText = message.originalText,
                translatedText = message.translatedText,
                sourceLangCode = message.sourceLanguage.iso639_1,
                targetLangCode = message.targetLanguage.iso639_1,
                timestamp = message.timestamp,
                latencyMs = message.latencyMs
            )
            historyRepository.saveTranscript(entity)
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioCaptureManager.stopCapture()
        audioPlaybackManager.release()
        audioRouter.reset()
    }
}
