package com.example.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import java.util.Locale

class TtsManager(
    private val context: Context,
    private val onSpeechStart: () -> Unit = {},
    private val onSpeechDone: () -> Unit = {}
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    val customVoiceManager = CustomVoiceManager(context)
    private val prefs = context.getSharedPreferences("jarvis_settings", Context.MODE_PRIVATE)

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    onSpeechStart()
                }

                override fun onDone(utteranceId: String?) {
                    onSpeechDone()
                }

                override fun onError(utteranceId: String?) {
                    onSpeechDone()
                }
            })
            setVoiceParameters()
        } else {
            Log.e("TtsManager", "TTS Initialization failed")
        }
    }

    /**
     * Retrieves list of installed system TTS voices.
     */
    fun getAvailableSystemVoices(): List<Voice> {
        if (!isInitialized || tts == null) return emptyList()
        return try {
            tts?.voices?.filter { !it.isNetworkConnectionRequired }?.toList() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Applies voice profile settings based on selected preset or custom parameters.
     */
    fun setVoiceParameters() {
        if (!isInitialized || tts == null) return

        val presetId = prefs.getString("voice_preset_id", VoiceConstants.PRESET_JARVIS_CLASSIC.id) ?: VoiceConstants.PRESET_JARVIS_CLASSIC.id
        val preset = VoiceConstants.ALL_PRESETS.find { it.id == presetId } ?: VoiceConstants.PRESET_JARVIS_CLASSIC

        var rate = preset.speechRate
        var pitch = preset.pitch

        if (preset.id == VoiceConstants.PRESET_CUSTOM.id) {
            rate = prefs.getFloat("speech_rate", 1.0f)
            pitch = prefs.getFloat("speech_pitch", 1.0f)
        }

        tts?.setSpeechRate(rate)
        tts?.setPitch(pitch)

        // Apply system voice override if set
        val savedVoiceName = prefs.getString("system_voice_name", null)
        if (!savedVoiceName.isNull_Empty()) {
            try {
                val matchingVoice = tts?.voices?.find { it.name == savedVoiceName }
                if (matchingVoice != null) {
                    tts?.voice = matchingVoice
                }
            } catch (e: Exception) {
                Log.e("TtsManager", "Failed to set custom system voice", e)
            }
        }
    }

    private fun String?.isNull_Empty(): Boolean = this == null || this.trim().isEmpty()

    fun speak(text: String, language: String) {
        val enableCustomClips = prefs.getBoolean("enable_custom_clips", true)

        // Step 1: Check if custom voice response clip exists for this phrase
        if (enableCustomClips) {
            val matchedKey = customVoiceManager.matchTextToResponseKey(text)
            if (matchedKey != null && customVoiceManager.hasCustomClip(matchedKey)) {
                tts?.stop()
                Log.d("TtsManager", "Playing custom recorded voice clip for key: $matchedKey")
                val played = customVoiceManager.playClip(
                    key = matchedKey,
                    onStart = onSpeechStart,
                    onCompletion = onSpeechDone
                )
                if (played) return
            }
        }

        // Step 2: Fallback to TextToSpeech engine
        customVoiceManager.stopPlayback()

        if (!isInitialized || tts == null) return

        val locale = if (language == "BN") Locale("bn", "BD") else Locale.US
        val result = tts?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            tts?.setLanguage(Locale.US)
        }

        setVoiceParameters()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "JARVIS_UTTERANCE_ID")
    }

    /**
     * Preview voice preset output.
     */
    fun previewPreset(preset: VoicePreset, language: String) {
        customVoiceManager.stopPlayback()
        if (!isInitialized || tts == null) return

        tts?.setSpeechRate(preset.speechRate)
        tts?.setPitch(preset.pitch)

        val sampleText = if (language == "BN") {
            "জার্ভিস ভয়েস সিন্থেসিস অনলাইন, স্যার।"
        } else {
            "J.A.R.V.I.S. voice synthesis online, Sir."
        }

        val locale = if (language == "BN") Locale("bn", "BD") else Locale.US
        tts?.setLanguage(locale)
        tts?.speak(sampleText, TextToSpeech.QUEUE_FLUSH, null, "JARVIS_PREVIEW_ID")
    }

    /**
     * Preview a custom voice clip.
     */
    fun previewCustomClip(key: String): Boolean {
        tts?.stop()
        return customVoiceManager.playClip(
            key = key,
            onStart = onSpeechStart,
            onCompletion = onSpeechDone
        )
    }

    fun stop() {
        customVoiceManager.stopPlayback()
        tts?.stop()
    }

    fun shutdown() {
        customVoiceManager.release()
        tts?.stop()
        tts?.shutdown()
    }
}

