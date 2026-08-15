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
import com.example.transport.RelayTransport
import com.example.transport.TransportPacket
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

    // Kept for UI compatibility. Gemini Live owns the active microphone and
    // model playback pipeline; these managers are not used for AI audio.
    val audioCaptureManager = AudioCaptureManager()
    val audioPlaybackManager = AudioPlaybackManager()
    val audioRouter = AudioRouter(application)
    val relayTransport = RelayTransport(viewModelScope)
    val translationEngine: TranslationEngine = GeminiLiveTranslateEngine(viewModelScope)

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

    // Consent starts false. MainActivity requests RECORD_AUDIO only when the
    // user enters the active translation screen.
    private val _hasMicConsent = MutableStateFlow(false)
    val hasMicConsent: StateFlow<Boolean> = _hasMicConsent.asStateFlow()

    private val _showPrivacyDialog = MutableStateFlow(false)
    val showPrivacyDialog: StateFlow<Boolean> = _showPrivacyDialog.asStateFlow()

    val recentSessions = historyRepository.allSessions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private var currentSessionId = UUID.randomUUID().toString()
    private var sessionStartTime = 0L

    init {
        viewModelScope.launch {
            relayTransport.incomingPackets.collect { packet ->
                if (packet is TransportPacket.EndSession) {
                    _audioActivity.value = AudioActivity.IDLE
                }
            }
        }

        viewModelScope.launch {
            translationEngine.liveTranscriptEvents.collect { event ->
                when (event) {
                    is TranscriptEvent.Final -> {
                        val message = TranscriptMessage(
                            speakerId = "local_user",
                            speakerName = _sessionConfig.value.userName,
                            isLocalUser = true,
                            originalText = event.originalText,
                            translatedText = event.translatedText,
                            sourceLanguage = event.sourceLanguage,
                            targetLanguage = event.targetLanguage,
                            latencyMs = event.latencyMs
                        )
                        addTranscriptMessage(message)
                        _audioActivity.value = AudioActivity.IDLE
                    }
                    is TranscriptEvent.Partial -> {
                        _audioActivity.value = AudioActivity.TRANSLATING
                    }
                    is TranscriptEvent.Error -> {
                        _audioActivity.value = AudioActivity.IDLE
                    }
                    null -> Unit
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
        _audioActivity.value = AudioActivity.IDLE

        viewModelScope.launch {
            val initialized = translationEngine.initializeSession(
                sourceLanguage = _sessionConfig.value.spokenLanguage,
                targetLanguage = _sessionConfig.value.heardLanguage,
                voiceStylePreservation = _sessionConfig.value.voicePitchTransferEnabled,
                echoTargetLanguage = _sessionConfig.value.echoTargetLanguage
            )
            if (!initialized) return@launch
            relayTransport.startLocalSession(_sessionConfig.value.heardLanguage)
            _currentScreen.value = AppScreen.ACTIVE_TRANSLATE
        }
    }

    // Remote pairing APIs are retained for compatibility with existing screens,
    // but the transport now reports that remote networking is not configured
    // instead of simulating a successful connection.
    fun startHostSession() {
        _pairingInfo.value = relayTransport.startHostSession(
            hostName = _sessionConfig.value.userName,
            hostLanguage = _sessionConfig.value.spokenLanguage
        )
        _currentScreen.value = AppScreen.HOST_PAIRING
    }

    fun joinWithCode(code: String, isSimulation: Boolean = false) {
        relayTransport.joinSession(
            code = code,
            userName = _sessionConfig.value.userName,
            userLanguage = _sessionConfig.value.spokenLanguage,
            isSimulation = false
        )
    }

    fun simulatePeerJoinInHost() {
        relayTransport.simulateHostPeerJoined(
            _pairingInfo.value?.sessionCode.orEmpty(),
            _sessionConfig.value.heardLanguage
        )
    }

    fun startPushToTalk() {
        if (!_hasMicConsent.value) {
            _showPrivacyDialog.value = true
            return
        }

        _audioActivity.value = AudioActivity.CAPTURING_LOCAL
        _visualizerState.value = AudioVisualizerState(
            isLive = true,
            amplitude = 0.5f,
            waveformFrequencies = List(16) { 0.5f }
        )

        viewModelScope.launch {
            translationEngine.startAudioConversation()
        }
    }

    fun stopPushToTalk() {
        translationEngine.stopAudioConversation()
        _audioActivity.value = AudioActivity.TRANSLATING
        _visualizerState.value = AudioVisualizerState(isLive = false, amplitude = 0f)
    }

    fun injectTestUtterance(bengaliText: String) {
        viewModelScope.launch {
            _audioActivity.value = AudioActivity.TRANSLATING
            try {
                val result = translationEngine.translateTextDirect(
                    text = bengaliText,
                    sourceLanguage = _sessionConfig.value.spokenLanguage,
                    targetLanguage = _sessionConfig.value.heardLanguage
                )
                addTranscriptMessage(
                    TranscriptMessage(
                        speakerId = "local_user",
                        speakerName = _sessionConfig.value.userName,
                        isLocalUser = true,
                        originalText = bengaliText,
                        translatedText = result.first,
                        sourceLanguage = _sessionConfig.value.spokenLanguage,
                        targetLanguage = _sessionConfig.value.heardLanguage,
                        latencyMs = 0
                    )
                )
            } catch (_: Exception) {
                // Deliberately do not insert fake translation content.
            } finally {
                _audioActivity.value = AudioActivity.IDLE
            }
        }
    }

    fun triggerSimulatedPeerTurn() {
        // No-op by design. Production builds must never fabricate a remote user.
    }

    fun toggleAudioRouting() {
        val newRoute = !_sessionConfig.value.audioRoutingSpeaker
        _sessionConfig.value = _sessionConfig.value.copy(audioRoutingSpeaker = newRoute)
        audioRouter.setSpeakerphoneOn(newRoute)
    }

    fun endSession() {
        val endTime = System.currentTimeMillis()
        val totalMsgs = _transcripts.value.size
        val peerName = when (val status = connectionStatus.value) {
            is ConnectionStatus.Connected -> status.peerName
            else -> "Gemini Live"
        }

        viewModelScope.launch {
            historyRepository.saveSession(
                SessionHistoryEntity(
                    sessionId = currentSessionId,
                    peerName = peerName,
                    sourceLanguageCode = _sessionConfig.value.spokenLanguage.iso639_1,
                    targetLanguageCode = _sessionConfig.value.heardLanguage.iso639_1,
                    startTimeMillis = if (sessionStartTime > 0) sessionStartTime else endTime,
                    endTimeMillis = endTime,
                    totalUtterances = totalMsgs,
                    summary = _transcripts.value.lastOrNull()?.translatedText ?: "Conversation finished"
                )
            )
        }

        translationEngine.stopAudioConversation()
        viewModelScope.launch { translationEngine.releaseSession() }
        relayTransport.disconnect("User ended session")
        _audioActivity.value = AudioActivity.IDLE
        _visualizerState.value = AudioVisualizerState()
        _currentScreen.value = AppScreen.HOME
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch { historyRepository.deleteSession(sessionId) }
    }

    fun clearAllHistory() {
        viewModelScope.launch { historyRepository.clearAllHistory() }
    }

    fun replayMessageAudio(message: TranscriptMessage) {
        // Live audio playback belongs to the active Gemini session. A history
        // item is not silently synthesized into audio.
    }

    private fun addTranscriptMessage(message: TranscriptMessage) {
        _transcripts.value = _transcripts.value + message
        viewModelScope.launch {
            historyRepository.saveTranscript(
                TranscriptEntity(
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
            )
        }
    }

    override fun onCleared() {
        translationEngine.stopAudioConversation()
        viewModelScope.launch { translationEngine.releaseSession() }
        audioCaptureManager.stopCapture()
        audioPlaybackManager.release()
        audioRouter.reset()
        super.onCleared()
    }
}
