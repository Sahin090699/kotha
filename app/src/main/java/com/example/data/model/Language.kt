package com.example.data.model

data class Language(
    val code: String,              // BCP-47 e.g. "bn-BD", "en-US"
    val iso639_1: String,          // Short code e.g. "bn", "en"
    val englishName: String,       // e.g. "Bengali"
    val nativeName: String,        // e.g. "বাংলা"
    val flag: String,              // Emoji flag e.g. "🇧🇩"
    val voiceName: String,         // Default prebuilt voice or style
    val sampleCodeSwitchedPhrases: List<String> = emptyList()
) {
    companion object {
        val Bengali = Language(
            code = "bn-IN",
            iso639_1 = "bn",
            englishName = "Bengali",
            nativeName = "বাংলা",
            flag = "🇧🇩",
            voiceName = "Aoede",
            sampleCodeSwitchedPhrases = listOf(
                "ভাই, meeting টা কি postpone হয়ে গেছে?",
                "আমি airport-এ পৌঁছে গেছি, luggage collect করছি।",
                "Project deadline টা extend করার কোনো chance আছে?",
                "আজকের lunch কি অর্ডার করব?"
            )
        )

        val English = Language(
            code = "en-US",
            iso639_1 = "en",
            englishName = "English",
            nativeName = "English (US)",
            flag = "🇺🇸",
            voiceName = "Puck",
            sampleCodeSwitchedPhrases = listOf(
                "Hello! How are you doing today?",
                "Can you confirm the meeting schedule for tomorrow?",
                "I have just reached the station."
            )
        )

        val Hindi = Language(
            code = "hi-IN",
            iso639_1 = "hi",
            englishName = "Hindi",
            nativeName = "हिन्दी",
            flag = "🇮🇳",
            voiceName = "Charon",
            sampleCodeSwitchedPhrases = listOf(
                "नमस्ते, आप कैसे हैं?",
                "क्या हम कल मिल सकते हैं?"
            )
        )

        val Spanish = Language(
            code = "es-ES",
            iso639_1 = "es",
            englishName = "Spanish",
            nativeName = "Español",
            flag = "🇪🇸",
            voiceName = "Kore"
        )

        val Arabic = Language(
            code = "ar-SA",
            iso639_1 = "ar",
            englishName = "Arabic",
            nativeName = "العربية",
            flag = "🇸🇦",
            voiceName = "Fenrir"
        )

        val French = Language(
            code = "fr-FR",
            iso639_1 = "fr",
            englishName = "French",
            nativeName = "Français",
            flag = "🇫🇷",
            voiceName = "Aoede"
        )

        val German = Language(
            code = "de-DE",
            iso639_1 = "de",
            englishName = "German",
            nativeName = "Deutsch",
            flag = "🇩🇪",
            voiceName = "Puck"
        )

        val Japanese = Language(
            code = "ja-JP",
            iso639_1 = "ja",
            englishName = "Japanese",
            nativeName = "日本語",
            flag = "🇯🇵",
            voiceName = "Kore"
        )

        val Mandarin = Language(
            code = "zh-CN",
            iso639_1 = "zh",
            englishName = "Mandarin",
            nativeName = "中文",
            flag = "🇨🇳",
            voiceName = "Fenrir"
        )

        val Urdu = Language(
            code = "ur-PK",
            iso639_1 = "ur",
            englishName = "Urdu",
            nativeName = "اردو",
            flag = "🇵🇰",
            voiceName = "Charon"
        )

        val supportedLanguages: List<Language> = listOf(
            Bengali,
            English,
            Hindi,
            Spanish,
            Arabic,
            French,
            German,
            Japanese,
            Mandarin,
            Urdu
        )

        fun findByCode(code: String): Language {
            return supportedLanguages.firstOrNull { it.iso639_1.equals(code, ignoreCase = true) || it.code.equals(code, ignoreCase = true) }
                ?: Bengali
        }
    }
}
