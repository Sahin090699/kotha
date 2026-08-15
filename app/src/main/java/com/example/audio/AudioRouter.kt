package com.example.audio

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.util.Log

class AudioRouter(private val context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    enum class Route {
        SPEAKER,
        EARPIECE,
        BLUETOOTH,
        WIRED_HEADSET
    }

    fun setSpeakerphoneOn(on: Boolean) {
        try {
            audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (on) {
                    val speakerDevice = audioManager?.availableCommunicationDevices?.firstOrNull {
                        it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
                    }
                    if (speakerDevice != null) {
                        audioManager?.setCommunicationDevice(speakerDevice)
                    }
                } else {
                    val earpieceDevice = audioManager?.availableCommunicationDevices?.firstOrNull {
                        it.type == AudioDeviceInfo.TYPE_BUILTIN_EARPIECE
                    }
                    if (earpieceDevice != null) {
                        audioManager?.setCommunicationDevice(earpieceDevice)
                    } else {
                        audioManager?.clearCommunicationDevice()
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                audioManager?.isSpeakerphoneOn = on
            }
        } catch (e: Exception) {
            Log.e("AudioRouter", "Failed to switch audio route", e)
        }
    }

    fun getCurrentRoute(): Route {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val device = audioManager?.communicationDevice
            if (device != null) {
                return when (device.type) {
                    AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> Route.SPEAKER
                    AudioDeviceInfo.TYPE_BUILTIN_EARPIECE -> Route.EARPIECE
                    AudioDeviceInfo.TYPE_BLUETOOTH_SCO, AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> Route.BLUETOOTH
                    AudioDeviceInfo.TYPE_WIRED_HEADSET, AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> Route.WIRED_HEADSET
                    else -> Route.SPEAKER
                }
            }
        }
        @Suppress("DEPRECATION")
        return if (audioManager?.isSpeakerphoneOn == true) Route.SPEAKER else Route.EARPIECE
    }

    fun reset() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                audioManager?.clearCommunicationDevice()
            }
            audioManager?.mode = AudioManager.MODE_NORMAL
        } catch (e: Exception) {
            Log.e("AudioRouter", "Error resetting audio mode", e)
        }
    }
}
