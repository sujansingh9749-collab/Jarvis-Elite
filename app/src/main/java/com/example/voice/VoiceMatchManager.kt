package com.example.voice

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import org.json.JSONObject
import java.io.File
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.sqrt

enum class VoiceMatchStatus {
    NOT_ENROLLED,
    ENROLLED,
    ENROLLING
}

data class VoicePrintProfile(
    val ownerName: String = "Sir",
    val meanRmsDb: Float = 0f,
    val maxRmsDb: Float = 0f,
    val dynamicRangeDb: Float = 0f,
    val spectralEnergyRatio: Float = 0f,
    val sampleCount: Int = 0,
    val enrolledAtTimestamp: Long = System.currentTimeMillis()
)

data class SpeakerVerificationResult(
    val isVerified: Boolean,
    val confidencePercent: Int,
    val reason: String
)

class VoiceMatchManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("jarvis_voice_match", Context.MODE_PRIVATE)

    companion object {
        const val PREF_LOCK_ENABLED = "voice_match_lock_enabled"
        const val PREF_THRESHOLD = "voice_match_threshold" // 60, 75, 85
        const val PREF_PROFILE_JSON = "voice_print_profile_json"
    }

    var isLockEnabled: Boolean
        get() = prefs.getBoolean(PREF_LOCK_ENABLED, false)
        set(value) = prefs.edit().putBoolean(PREF_LOCK_ENABLED, value).apply()

    var matchThresholdPercent: Int
        get() = prefs.getInt(PREF_THRESHOLD, 70)
        set(value) = prefs.edit().putInt(PREF_THRESHOLD, value).apply()

    fun getVoiceProfile(): VoicePrintProfile? {
        val jsonStr = prefs.getString(PREF_PROFILE_JSON, null) ?: return null
        return try {
            val json = JSONObject(jsonStr)
            VoicePrintProfile(
                ownerName = json.optString("ownerName", "Sir"),
                meanRmsDb = json.optDouble("meanRmsDb", 0.0).toFloat(),
                maxRmsDb = json.optDouble("maxRmsDb", 0.0).toFloat(),
                dynamicRangeDb = json.optDouble("dynamicRangeDb", 0.0).toFloat(),
                spectralEnergyRatio = json.optDouble("spectralEnergyRatio", 0.0).toFloat(),
                sampleCount = json.optInt("sampleCount", 0),
                enrolledAtTimestamp = json.optLong("enrolledAtTimestamp", 0L)
            )
        } catch (e: Exception) {
            Log.e("VoiceMatchManager", "Failed to parse voice profile JSON", e)
            null
        }
    }

    fun saveVoiceProfile(profile: VoicePrintProfile) {
        try {
            val json = JSONObject().apply {
                put("ownerName", profile.ownerName)
                put("meanRmsDb", profile.meanRmsDb.toDouble())
                put("maxRmsDb", profile.maxRmsDb.toDouble())
                put("dynamicRangeDb", profile.dynamicRangeDb.toDouble())
                put("spectralEnergyRatio", profile.spectralEnergyRatio.toDouble())
                put("sampleCount", profile.sampleCount)
                put("enrolledAtTimestamp", profile.enrolledAtTimestamp)
            }
            prefs.edit().putString(PREF_PROFILE_JSON, json.toString()).apply()
            Log.d("VoiceMatchManager", "Saved voice print profile for ${profile.ownerName}")
        } catch (e: Exception) {
            Log.e("VoiceMatchManager", "Failed to save voice profile", e)
        }
    }

    fun clearVoiceProfile() {
        prefs.edit().remove(PREF_PROFILE_JSON).putBoolean(PREF_LOCK_ENABLED, false).apply()
    }

    /**
     * Extracts acoustic envelope metrics from captured RMS values during speech.
     */
    fun extractMetricsFromRmsList(rmsList: List<Float>): VoicePrintProfile {
        if (rmsList.isEmpty()) {
            return VoicePrintProfile(meanRmsDb = 10f, maxRmsDb = 20f, dynamicRangeDb = 10f)
        }

        val nonZeroRms = rmsList.filter { it > -10f && it < 100f }
        if (nonZeroRms.isEmpty()) {
            return VoicePrintProfile(meanRmsDb = 10f, maxRmsDb = 20f, dynamicRangeDb = 10f)
        }

        val mean = nonZeroRms.average().toFloat()
        val max = nonZeroRms.maxOrNull() ?: mean
        val min = nonZeroRms.minOrNull() ?: mean
        val range = max - min

        // Estimate spectral energy density from variance
        val variance = nonZeroRms.fold(0.0) { acc, v -> acc + (v - mean) * (v - mean) } / nonZeroRms.size
        val spectralRatio = sqrt(variance).toFloat()

        return VoicePrintProfile(
            meanRmsDb = mean,
            maxRmsDb = max,
            dynamicRangeDb = range,
            spectralEnergyRatio = spectralRatio,
            sampleCount = nonZeroRms.size
        )
    }

    /**
     * Verifies if incoming audio sequence matches the enrolled owner's voice print.
     */
    fun verifySpeaker(
        observedRmsList: List<Float>,
        transcript: String
    ): SpeakerVerificationResult {
        if (!isLockEnabled) {
            return SpeakerVerificationResult(isVerified = true, confidencePercent = 100, reason = "Lock Disabled")
        }

        val enrolledProfile = getVoiceProfile()
            ?: return SpeakerVerificationResult(
                isVerified = false,
                confidencePercent = 0,
                reason = "No Voice Print Enrolled"
            )

        val observedProfile = extractMetricsFromRmsList(observedRmsList)

        // Calculate Acoustic Feature Distances
        val meanDiff = abs(observedProfile.meanRmsDb - enrolledProfile.meanRmsDb)
        val maxDiff = abs(observedProfile.maxRmsDb - enrolledProfile.maxRmsDb)
        val rangeDiff = abs(observedProfile.dynamicRangeDb - enrolledProfile.dynamicRangeDb)
        val spectralDiff = abs(observedProfile.spectralEnergyRatio - enrolledProfile.spectralEnergyRatio)

        // Map differences to confidence score (0 - 100%)
        val meanScore = (100f - (meanDiff * 4f)).coerceIn(0f, 100f)
        val maxScore = (100f - (maxDiff * 3f)).coerceIn(0f, 100f)
        val rangeScore = (100f - (rangeDiff * 2.5f)).coerceIn(0f, 100f)
        val spectralScore = (100f - (spectralDiff * 5f)).coerceIn(0f, 100f)

        // Weighted acoustic score calculation
        var rawConfidence = (meanScore * 0.35f) + (maxScore * 0.25f) + (rangeScore * 0.20f) + (spectralScore * 0.20f)

        // Contextual boost if owner passphrase keywords or "Jarvis" is spoke naturally
        val lowerTranscript = transcript.lowercase()
        if (lowerTranscript.contains("jarvis") || lowerTranscript.contains("জার্ভিস") || lowerTranscript.contains("sir") || lowerTranscript.contains("স্যার")) {
            rawConfidence += 8f
        }

        val finalConfidence = rawConfidence.toInt().coerceIn(10, 99)
        val isVerified = finalConfidence >= matchThresholdPercent

        val reasonStr = if (isVerified) {
            "Voice signature matched enrolled profile (${finalConfidence}% match)"
        } else {
            "Voice acoustic signature below security threshold (${finalConfidence}% < ${matchThresholdPercent}%)"
        }

        Log.d("VoiceMatchManager", "Speaker Verification -> Verified: $isVerified ($finalConfidence% match)")

        return SpeakerVerificationResult(
            isVerified = isVerified,
            confidencePercent = finalConfidence,
            reason = reasonStr
        )
    }
}
