package com.example.transport

import android.util.Log
import com.example.data.model.ConnectionQuality
import com.example.data.model.ConnectionStatus
import com.example.data.model.Language
import com.example.data.model.TranscriptMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface TransportPacket {
    data class AudioPayload(val audioData: ByteArray, val sampleRate: Int = 24000) : TransportPacket
    data class TranscriptPayload(val message: TranscriptMessage) : TransportPacket
    data class Heartbeat(val timestamp: Long) : TransportPacket
    data class EndSession(val reason: String) : TransportPacket
}

/**
 * Session transport abstraction.
 *
 * Gemini Live is responsible for the single-device AI conversation. This
 * class intentionally does not pretend to be a remote-device transport.
 * Host/join remains a reserved integration point until a real authenticated
 * signaling/relay service is deployed.
 */
class RelayTransport(private val scope: CoroutineScope) {
    private val _connectionStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Disconnected)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _incomingPackets = MutableSharedFlow<TransportPacket>(extraBufferCapacity = 64)
    val incomingPackets: SharedFlow<TransportPacket> = _incomingPackets.asSharedFlow()

    private var heartbeatJob: Job? = null

    fun startLocalSession(userLanguage: Language) {
        _connectionStatus.value = ConnectionStatus.Connected(
            sessionCode = "LOCAL",
            peerName = "Gemini Live",
            peerLanguage = userLanguage,
            connectionQuality = ConnectionQuality.EXCELLENT
        )
    }

    fun startHostSession(hostName: String, hostLanguage: Language): PairingInfo {
        val pairing = PairingManager.generateSession(hostName, hostLanguage)
        _connectionStatus.value = ConnectionStatus.Ended(
            "Remote 1:1 sessions are not enabled in this build."
        )
        return pairing
    }

    fun joinSession(code: String, userName: String, userLanguage: Language, isSimulation: Boolean = false) {
        _connectionStatus.value = ConnectionStatus.Ended(
            "Remote pairing is not available until a real signaling service is configured."
        )
    }

    fun simulateHostPeerJoined(sessionCode: String, peerLanguage: Language) {
        _connectionStatus.value = ConnectionStatus.Ended(
            "Simulation mode has been removed from the production transport."
        )
    }

    fun sendAudioToPeer(audioData: ByteArray) {
        // Reserved for the future authenticated peer transport.
    }

    fun sendTranscriptToPeer(message: TranscriptMessage) {
        // Reserved for the future authenticated peer transport.
    }

    fun triggerPeerResponse(
        peerBengaliText: String,
        translatedEnglishText: String,
        sourceLang: Language,
        targetLang: Language,
        audioPcm: ByteArray?
    ) {
        // Simulation intentionally removed. Never manufacture peer messages.
        Log.w("RelayTransport", "Ignoring simulated peer response request")
    }

    fun disconnect(reason: String = "User left session") {
        heartbeatJob?.cancel()
        heartbeatJob = null
        _connectionStatus.value = ConnectionStatus.Ended(reason)
        scope.launch { _incomingPackets.emit(TransportPacket.EndSession(reason)) }
    }

    fun resetToIdle() {
        heartbeatJob?.cancel()
        heartbeatJob = null
        _connectionStatus.value = ConnectionStatus.Disconnected
    }
}
