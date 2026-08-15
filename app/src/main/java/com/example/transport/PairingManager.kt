package com.example.transport

import com.example.data.model.Language
import java.security.SecureRandom

data class PairingInfo(
    val sessionCode: String,
    val hostName: String,
    val hostLanguage: Language,
    val expiresAtMillis: Long,
    val qrPayload: String
) {
    val isExpired: Boolean
        get() = System.currentTimeMillis() > expiresAtMillis

    val remainingSeconds: Long
        get() = ((expiresAtMillis - System.currentTimeMillis()) / 1000).coerceAtLeast(0)
}

object PairingManager {
    private const val CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // 32 characters, no 0/O/1/I
    private const val CODE_LENGTH = 6
    private const val SESSION_TTL_MILLIS = 10 * 60 * 1000L // 10 minutes
    private val random = SecureRandom()

    fun generateSession(hostName: String, hostLanguage: Language): PairingInfo {
        val sb = StringBuilder(CODE_LENGTH)
        for (i in 0 until CODE_LENGTH) {
            sb.append(CODE_CHARS[random.nextInt(CODE_CHARS.length)])
        }
        val code = sb.toString()
        val expiresAt = System.currentTimeMillis() + SESSION_TTL_MILLIS
        val payload = "kotha://pair?code=$code&host=${hostName.trim()}&lang=${hostLanguage.iso639_1}"

        return PairingInfo(
            sessionCode = code,
            hostName = hostName,
            hostLanguage = hostLanguage,
            expiresAtMillis = expiresAt,
            qrPayload = payload
        )
    }

    fun parseQrPayload(payload: String): Pair<String, String>? {
        if (!payload.startsWith("kotha://pair")) {
            // Check if it's just raw 6-character code
            val cleaned = payload.trim().uppercase()
            if (cleaned.length == 6 && cleaned.all { it.isLetterOrDigit() }) {
                return Pair(cleaned, "bn")
            }
            return null
        }

        try {
            val queryParams = payload.substringAfter("?", "")
            val pairs = queryParams.split("&").associate {
                val parts = it.split("=")
                if (parts.size == 2) parts[0] to parts[1] else "" to ""
            }
            val code = pairs["code"] ?: return null
            val lang = pairs["lang"] ?: "bn"
            return Pair(code.uppercase(), lang)
        } catch (e: Exception) {
            return null
        }
    }
}
