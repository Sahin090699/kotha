package com.example.engine

import com.example.data.model.Language

/** Result of a translation request. Failed requests are represented explicitly. */
data class TranslationResult(
    val originalText: String,
    val translatedText: String,
    val sourceLanguage: Language,
    val targetLanguage: Language,
    val latencyMs: Long,
    val audioData: ByteArray? = null
)
