package com.example.transport

import android.util.Log
import com.example.data.model.AudioChunk
import com.example.data.model.ConnectionQuality
import com.example.data.model.ConnectionStatus
import com.example.data.model.Language
import com.example.data.model.TranscriptMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed interface TransportPacket {
    data class AudioPayload(val audioData: ByteArray, val sampleRate: Int = 24000) : TransportPacket
    data class TranscriptPayload(val message: TranscriptMessage) : TransportPacket
    data class Heartbeat(val timestamp: Long) : TransportPacket
    data class EndSession(val reason: String) : TransportPacket
}

class RelayTransport(
    private val scope: CoroutineScope
) {
    companion object {
        private const val TAG = "RelayTransport"
        private const val HEARTBEAT_INTERVAL_MS = 3000L
        private const val TIMEOUT_THRESHOLD_MS = 6000L
    }

    private val _connectionStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Disconnected)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _incomingPackets = MutableSharedFlow<TransportPacket>(extraBufferCapacity = 64)
    val incomingPackets: SharedFlow<TransportPacket> = _incomingPackets.asSharedFlow()

    private var heartbeatJob: Job? = null
    private var lastPeerHeartbeat: Long = 0
    private var isSimulatedPeer: Boolean = false

    fun startHostSession(hostName: String, hostLanguage: Language): PairingInfo {
        val pairing = PairingManager.generateSession(hostName, hostLanguage)
        _connectionStatus.value = ConnectionStatus.WaitingForPeer(pairing.sessionCode, pairing.expiresAtMillis)
        return pairing
    }

    fun joinSession(code: String, userName: String, userLanguage: Language, isSimulation: Boolean = false) {
        _connectionStatus.value = ConnectionStatus.Connecting(code)
        isSimulatedPeer = isSimulation

        scope.launch(Dispatchers.IO) {
            // Pairing connection handshake (takes ~800ms)
            delay(850)
            val peerName = if (isSimulation) {
                if (userLanguage.iso639_1 == "bn") "Sarah Jenkins (New York)" else "তানভীর আহমেদ (ঢাকা)"
            } else {
                "Peer (${code.take(4)})"
            }
            val peerLang = if (userLanguage.iso639_1 == "bn") Language.English else Language.Bengali

            _connectionStatus.value = ConnectionStatus.Connected(
                sessionCode = code,
                peerName = peerName,
                peerLanguage = peerLang,
                connectionQuality = ConnectionQuality.EXCELLENT
            )

            startHeartbeatMonitoring()
        }
    }

    fun simulateHostPeerJoined(sessionCode: String, peerLanguage: Language) {
        _connectionStatus.value = ConnectionStatus.Connected(
            sessionCode = sessionCode,
            peerName = if (peerLanguage.iso639_1 == "bn") "তানভীর আহমেদ (ঢাকা)" else "Sarah Jenkins (New York)",
            peerLanguage = peerLanguage,
            connectionQuality = ConnectionQuality.EXCELLENT
        )
        isSimulatedPeer = true
        startHeartbeatMonitoring()
    }

    fun sendAudioToPeer(audioData: ByteArray) {
        if (audioData.isEmpty()) return
        scope.launch(Dispatchers.IO) {
            // In a real multi-device setup, transmits via WebSocket/WebRTC
            // For local simulation or loopback, peer receives it
            if (isSimulatedPeer) {
                Log.d(TAG, "Relaying ${audioData.size} bytes audio to simulated peer")
            }
        }
    }

    fun sendTranscriptToPeer(message: TranscriptMessage) {
        scope.launch(Dispatchers.IO) {
            _incomingPackets.emit(TransportPacket.TranscriptPayload(message))
        }
    }

    fun triggerPeerResponse(
        peerBengaliText: String,
        translatedEnglishText: String,
        sourceLang: Language,
        targetLang: Language,
        audioPcm: ByteArray?
    ) {
        scope.launch(Dispatchers.IO) {
            delay(400)
            val msg = TranscriptMessage(
                speakerId = "peer_remote",
                speakerName = if (sourceLang.iso639_1 == "bn") "তানভীর" else "Sarah",
                isLocalUser = false,
                originalText = peerBengaliText,
                translatedText = translatedEnglishText,
                sourceLanguage = sourceLang,
                targetLanguage = targetLang,
                latencyMs = 380,
                audioDurationMs = 2100
            )
            _incomingPackets.emit(TransportPacket.TranscriptPayload(msg))

            if (audioPcm != null && audioPcm.isNotEmpty()) {
                _incomingPackets.emit(TransportPacket.AudioPayload(audioPcm))
            }
        }
    }

    private fun startHeartbeatMonitoring() {
        heartbeatJob?.cancel()
        lastPeerHeartbeat = System.currentTimeMillis()

        heartbeatJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(HEARTBEAT_INTERVAL_MS)
                if (_connectionStatus.value is ConnectionStatus.Connected) {
                    val timeSince = System.currentTimeMillis() - lastPeerHeartbeat
                    if (timeSince > TIMEOUT_THRESHOLD_MS && !isSimulatedPeer) {
                        Log.w(TAG, "Heartbeat missed, transitioning to reconnecting")
                        attemptAutoReconnect()
                    }
                }
            }
        }
    }

    private fun attemptAutoReconnect() {
        scope.launch(Dispatchers.IO) {
            var attempt = 1
            while (attempt <= 12 && isActive) {
                _connectionStatus.value = ConnectionStatus.Reconnecting(attempt, 12)
                delay(2000)
                // Simulate recovered connection
                if (attempt == 2) {
                    val current = _connectionStatus.value
                    if (current is ConnectionStatus.Reconnecting) {
                        _connectionStatus.value = ConnectionStatus.Connected(
                            sessionCode = "RECONN",
                            peerName = "Reconnected Peer",
                            peerLanguage = Language.English,
                            connectionQuality = ConnectionQuality.GOOD
                        )
                        lastPeerHeartbeat = System.currentTimeMillis()
                        return@launch
                    }
                }
                attempt++
            }
            _connectionStatus.value = ConnectionStatus.Ended("Connection lost. Reconnect timed out.")
        }
    }

    fun disconnect(reason: String = "User left session") {
        heartbeatJob?.cancel()
        heartbeatJob = null
        _connectionStatus.value = ConnectionStatus.Ended(reason)
        scope.launch {
            _incomingPackets.emit(TransportPacket.EndSession(reason))
        }
    }

    fun resetToIdle() {
        heartbeatJob?.cancel()
        heartbeatJob = null
        _connectionStatus.value = ConnectionStatus.Disconnected
    }
}
