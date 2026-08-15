package com.example.data.model

import java.util.UUID

sealed interface ConnectionStatus {
    object Disconnected : ConnectionStatus
    data class GeneratingCode(val requestedBy: String) : ConnectionStatus
    data class WaitingForPeer(val sessionCode: String, val expiresAtMillis: Long) : ConnectionStatus
    data class Connecting(val sessionCode: String) : ConnectionStatus
    data class Connected(
        val sessionCode: String,
        val peerName: String,
        val peerLanguage: Language,
        val connectionQuality: ConnectionQuality = ConnectionQuality.EXCELLENT
    ) : ConnectionStatus
    data class Reconnecting(val attempt: Int, val maxAttempts: Int = 12) : ConnectionStatus
    data class Ended(val reason: String) : ConnectionStatus
}

enum class ConnectionQuality {
    EXCELLENT, GOOD, POOR
}

enum class AudioActivity {
    IDLE,
    CAPTURING_LOCAL,    // Local user speaking (Push-to-talk held)
    TRANSLATING,        // Stream sending & awaiting live translation response
    PLAYING_PEER_AUDIO  // Listening to peer's translated audio stream
}

data class TranscriptMessage(
    val id: String = UUID.randomUUID().toString(),
    val speakerId: String,
    val speakerName: String,
    val isLocalUser: Boolean,
    val originalText: String,
    val translatedText: String,
    val sourceLanguage: Language,
    val targetLanguage: Language,
    val timestamp: Long = System.currentTimeMillis(),
    val isPartial: Boolean = false,
    val latencyMs: Long = 0,
    val audioDurationMs: Long = 0
)

data class SessionConfig(
    val userName: String = "User",
    val spokenLanguage: Language = Language.Bengali,
    val heardLanguage: Language = Language.English,
    val isPushToTalk: Boolean = true,
    val noiseGateThreshold: Float = 0.05f,
    val echoTargetLanguage: Boolean = false, // Echo original if already target language or silence
    val manualLanguageOverride: Boolean = false,
    val audioRoutingSpeaker: Boolean = true, // true = Speaker, false = Earpiece
    val isSimulationMode: Boolean = false,   // 2-way testing mode for standalone demos
    val voicePitchTransferEnabled: Boolean = true
)
