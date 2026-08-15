package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session_history")
data class SessionHistoryEntity(
    @PrimaryKey val sessionId: String,
    val peerName: String,
    val sourceLanguageCode: String,
    val targetLanguageCode: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long = 0,
    val totalUtterances: Int = 0,
    val summary: String = ""
)

@Entity(tableName = "transcript_records")
data class TranscriptEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val speakerName: String,
    val isLocalUser: Boolean,
    val originalText: String,
    val translatedText: String,
    val sourceLangCode: String,
    val targetLangCode: String,
    val timestamp: Long,
    val latencyMs: Long
)
