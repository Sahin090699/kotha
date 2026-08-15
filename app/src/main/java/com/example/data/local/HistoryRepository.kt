package com.example.data.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class HistoryRepository(private val sessionDao: SessionDao) {

    val allSessions: Flow<List<SessionHistoryEntity>> = sessionDao.getAllSessions()

    fun getTranscriptsForSession(sessionId: String): Flow<List<TranscriptEntity>> {
        return sessionDao.getTranscriptsForSession(sessionId)
    }

    suspend fun saveSession(session: SessionHistoryEntity) = withContext(Dispatchers.IO) {
        sessionDao.insertSession(session)
    }

    suspend fun updateSessionEnd(sessionId: String, endTime: Long, count: Int) = withContext(Dispatchers.IO) {
        sessionDao.updateSessionEnd(sessionId, endTime, count)
    }

    suspend fun saveTranscript(transcript: TranscriptEntity) = withContext(Dispatchers.IO) {
        sessionDao.insertTranscript(transcript)
    }

    suspend fun deleteSession(sessionId: String) = withContext(Dispatchers.IO) {
        sessionDao.deleteTranscriptsForSession(sessionId)
        sessionDao.deleteSession(sessionId)
    }

    suspend fun clearAllHistory() = withContext(Dispatchers.IO) {
        sessionDao.deleteAllTranscripts()
        sessionDao.deleteAllHistory()
    }
}
