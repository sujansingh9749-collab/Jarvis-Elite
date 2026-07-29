package com.example.voice

data class VoicePreset(
    val id: String,
    val name: String,
    val description: String,
    val pitch: Float,
    val speechRate: Float
)

data class ResponseKeyInfo(
    val key: String,
    val title: String,
    val description: String,
    val defaultTextEn: String,
    val defaultTextBn: String
)

object VoiceConstants {
    val PRESET_JARVIS_CLASSIC = VoicePreset(
        id = "jarvis_classic",
        name = "J.A.R.V.I.S. Classic",
        description = "Deep, composed British AI style",
        pitch = 0.85f,
        speechRate = 1.05f
    )

    val PRESET_FRIDAY_CYBER = VoicePreset(
        id = "friday_cyber",
        name = "F.R.I.D.A.Y. Cyber",
        description = "Crisp, fast tactical assistant",
        pitch = 1.25f,
        speechRate = 1.10f
    )

    val PRESET_EDITH_PRECISION = VoicePreset(
        id = "edith_precision",
        name = "E.D.I.T.H. Tactical",
        description = "High-speed precise system output",
        pitch = 0.95f,
        speechRate = 1.25f
    )

    val PRESET_VISION_ECHO = VoicePreset(
        id = "vision_echo",
        name = "V.I.S.I.O.N. Resonant",
        description = "Deep resonant synthetic tone",
        pitch = 0.65f,
        speechRate = 0.90f
    )

    val PRESET_CUSTOM = VoicePreset(
        id = "custom_user",
        name = "Custom Sliders",
        description = "User fine-tuned pitch and speech speed",
        pitch = 1.00f,
        speechRate = 1.00f
    )

    val ALL_PRESETS = listOf(
        PRESET_JARVIS_CLASSIC,
        PRESET_FRIDAY_CYBER,
        PRESET_EDITH_PRECISION,
        PRESET_VISION_ECHO,
        PRESET_CUSTOM
    )

    val RESPONSE_KEYS = listOf(
        ResponseKeyInfo(
            key = "GREETING",
            title = "Greeting & Core Ready",
            description = "Played when J.A.R.V.I.S. boots or greets the user.",
            defaultTextEn = "Sir, J.A.R.V.I.S. online and ready.",
            defaultTextBn = "স্যার, জার্ভিস সিস্টেম সম্পূর্ণ প্রস্তুত।"
        ),
        ResponseKeyInfo(
            key = "AFFIRMATION",
            title = "Affirmation / Confirmation",
            description = "Acknowledging commands with 'Yes, Sir' or 'Right away'.",
            defaultTextEn = "Yes, Sir. Executing immediately.",
            defaultTextBn = "জি স্যার, অবিলম্বে বাস্তবায়ন করা হচ্ছে।"
        ),
        ResponseKeyInfo(
            key = "FLASHLIGHT",
            title = "Flashlight Toggle Response",
            description = "Spoken when turning flashlight on or off.",
            defaultTextEn = "Flashlight activated, Sir.",
            defaultTextBn = "ফ্ল্যাশলাইট চালু করা হয়েছে, স্যার।"
        ),
        ResponseKeyInfo(
            key = "VOLUME",
            title = "Volume Control Response",
            description = "Spoken when adjusting device volume.",
            defaultTextEn = "Adjusting audio levels, Sir.",
            defaultTextBn = "ভলিউম পরিবর্তন করা হচ্ছে, স্যার।"
        ),
        ResponseKeyInfo(
            key = "LANGUAGE_CHANGED",
            title = "Language Switch Response",
            description = "Spoken when switching between English and Bengali.",
            defaultTextEn = "Language profile updated, Sir.",
            defaultTextBn = "ভাষা পরিবর্তন করা হয়েছে, স্যার।"
        ),
        ResponseKeyInfo(
            key = "SYSTEM_READY",
            title = "Diagnostics / System Status",
            description = "Spoken after running self-diagnostics.",
            defaultTextEn = "Diagnostic report complete. All systems nominal.",
            defaultTextBn = "ডায়াগনস্টিক সম্পূর্ণ। সকল সিস্টেম স্বাভাবিক।"
        ),
        ResponseKeyInfo(
            key = "SMART_PROTOCOL",
            title = "5. Smart Protocol / Voice Automation",
            description = "Spoken when executing macros like Stealth Protocol or Defense Mode.",
            defaultTextEn = "Smart protocol engaged, Sir. All parameters adjusted.",
            defaultTextBn = "স্মার্ট প্রটোকল সক্রিয় করা হয়েছে, স্যার।"
        ),
        ResponseKeyInfo(
            key = "CONTEXTUAL_REMINDER",
            title = "6. Smart Reminders & Intelligent Alarms",
            description = "Spoken when setting contextual reminders or smart schedules.",
            defaultTextEn = "Contextual reminder scheduled, Sir.",
            defaultTextBn = "স্মার্ট রিমাইন্ডার এবং অ্যালার্ম শিডিউল করা হয়েছে, স্যার।"
        ),
        ResponseKeyInfo(
            key = "ACCESS_DENIED",
            title = "Voice Security / Access Denied",
            description = "Spoken when voice print signature fails security verification.",
            defaultTextEn = "Access Denied, Sir. Voice print signature mismatch.",
            defaultTextBn = "অ্যাক্সেস প্রত্যাখ্যান করা হয়েছে। ভয়েস প্রিন্ট মেলেনি।"
        ),
        ResponseKeyInfo(
            key = "ERROR_FALLBACK",
            title = "Error / Unrecognized Command",
            description = "Spoken when a command fails or cannot be parsed.",
            defaultTextEn = "Unable to complete request, Sir.",
            defaultTextBn = "অনুরোধ সম্পন্ন করা সম্ভব হয়নি, স্যার।"
        )
    )
}
