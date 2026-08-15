package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM session_history ORDER BY startTimeMillis DESC")
    fun getAllSessions(): Flow<List<SessionHistoryEntity>>

    @Query("SELECT * FROM session_history WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: String): SessionHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionHistoryEntity)

    @Query("UPDATE session_history SET endTimeMillis = :endTime, totalUtterances = :totalUtterances WHERE sessionId = :sessionId")
    suspend fun updateSessionEnd(sessionId: String, endTime: Long, totalUtterances: Int)

    @Query("SELECT * FROM transcript_records WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getTranscriptsForSession(sessionId: String): Flow<List<TranscriptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranscript(transcript: TranscriptEntity)

    @Query("DELETE FROM session_history WHERE sessionId = :sessionId")
    suspend fun deleteSession(sessionId: String)

    @Query("DELETE FROM transcript_records WHERE sessionId = :sessionId")
    suspend fun deleteTranscriptsForSession(sessionId: String)

    @Transaction
    @Query("DELETE FROM session_history")
    suspend fun deleteAllHistory()

    @Transaction
    @Query("DELETE FROM transcript_records")
    suspend fun deleteAllTranscripts()
}
