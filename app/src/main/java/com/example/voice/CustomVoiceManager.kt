package com.example.voice

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class CustomVoiceManager(private val context: Context) {

    private val voiceDir = File(context.filesDir, "voice_clips").apply {
        if (!exists()) mkdirs()
    }

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var recordingKey: String? = null

    val isRecording: Boolean
        get() = mediaRecorder != null

    val currentRecordingKey: String?
        get() = recordingKey

    /**
     * Checks if a custom audio clip file exists for the given response key.
     */
    fun hasCustomClip(key: String): Boolean {
        val file = getClipFile(key)
        return file.exists() && file.length() > 0
    }

    /**
     * Retrieves the file object for a given response key.
     */
    fun getClipFile(key: String): File {
        return File(voiceDir, "${key.lowercase()}.m4a")
    }

    /**
     * Starts recording a custom audio clip for a specific response key.
     */
    fun startRecording(key: String): Boolean {
        stopRecording()
        stopPlayback()

        val outFile = getClipFile(key)
        if (outFile.exists()) {
            outFile.delete()
        }

        return try {
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            recorder.setAudioEncodingBitRate(128000)
            recorder.setAudioSamplingRate(44100)
            recorder.setOutputFile(outFile.absolutePath)
            recorder.prepare()
            recorder.start()

            mediaRecorder = recorder
            recordingKey = key
            Log.d("CustomVoiceManager", "Started recording clip for key: $key")
            true
        } catch (e: Exception) {
            Log.e("CustomVoiceManager", "Failed to start recording", e)
            mediaRecorder = null
            recordingKey = null
            false
        }
    }

    /**
     * Stops current recording and saves file.
     */
    fun stopRecording(): File? {
        val key = recordingKey ?: return null
        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            recordingKey = null
            val recordedFile = getClipFile(key)
            if (recordedFile.exists() && recordedFile.length() > 0) {
                Log.d("CustomVoiceManager", "Recording saved: ${recordedFile.absolutePath}")
                recordedFile
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("CustomVoiceManager", "Failed to stop recording cleanly", e)
            mediaRecorder = null
            recordingKey = null
            getClipFile(key).takeIf { it.exists() && it.length() > 0 }
        }
    }

    /**
     * Imports/Uploads a custom audio clip file from URI selected by user.
     */
    fun saveUploadedClip(key: String, uri: Uri): Boolean {
        stopPlayback()
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            if (inputStream == null) return false

            val outFile = getClipFile(key)
            if (outFile.exists()) outFile.delete()

            FileOutputStream(outFile).use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()
            Log.d("CustomVoiceManager", "Uploaded audio clip saved for $key: ${outFile.length()} bytes")
            true
        } catch (e: Exception) {
            Log.e("CustomVoiceManager", "Error saving uploaded clip for $key", e)
            false
        }
    }

    /**
     * Deletes custom clip file for key.
     */
    fun deleteClip(key: String): Boolean {
        stopPlayback()
        val file = getClipFile(key)
        return if (file.exists()) {
            file.delete()
        } else false
    }

    /**
     * Plays a custom voice clip audio file using MediaPlayer.
     */
    fun playClip(
        key: String,
        onStart: () -> Unit = {},
        onCompletion: () -> Unit = {}
    ): Boolean {
        stopPlayback()
        val file = getClipFile(key)
        if (!file.exists() || file.length() == 0L) return false

        return try {
            val player = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                setOnPreparedListener { mp ->
                    onStart()
                    mp.start()
                }
                setOnCompletionListener { mp ->
                    mp.release()
                    mediaPlayer = null
                    onCompletion()
                }
                setOnErrorListener { mp, _, _ ->
                    mp.release()
                    mediaPlayer = null
                    onCompletion()
                    true
                }
                prepareAsync()
            }
            mediaPlayer = player
            true
        } catch (e: Exception) {
            Log.e("CustomVoiceManager", "Error playing clip for $key", e)
            mediaPlayer = null
            onCompletion()
            false
        }
    }

    /**
     * Stops any currently playing clip.
     */
    fun stopPlayback() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            Log.e("CustomVoiceManager", "Error stopping player", e)
        } finally {
            mediaPlayer = null
        }
    }

    /**
     * Matches spoken text to standard response keys.
     */
    fun matchTextToResponseKey(text: String): String? {
        val lower = text.lowercase()
        return when {
            lower.contains("online and ready") || lower.contains("সিস্টেম সম্পূর্ণ প্রস্তুত") || lower.contains("ready, sir") || lower.contains("online, sir") -> "GREETING"
            lower.contains("yes, sir") || lower.contains("জি স্যার") || lower.contains("right away") || lower.contains("immediately") -> "AFFIRMATION"
            lower.contains("flashlight") || lower.contains("ফ্ল্যাশলাইট") -> "FLASHLIGHT"
            lower.contains("volume") || lower.contains("ভলিউম") -> "VOLUME"
            lower.contains("language") || lower.contains("ভাষা") -> "LANGUAGE_CHANGED"
            lower.contains("diagnostic") || lower.contains("ডায়াগনস্টিক") || lower.contains("diagnostics complete") -> "SYSTEM_READY"
            lower.contains("protocol") || lower.contains("প্রটোকল") || lower.contains("stealth") || lower.contains("defense") -> "SMART_PROTOCOL"
            lower.contains("reminder") || lower.contains("রিমাইন্ডার") || lower.contains("alarm set") || lower.contains("অ্যালার্ম সেট") -> "CONTEXTUAL_REMINDER"
            lower.contains("access denied") || lower.contains("অ্যাক্সেস প্রত্যাখ্যান") || lower.contains("signature mismatch") -> "ACCESS_DENIED"
            lower.contains("unable to") || lower.contains("সম্পন্ন করা সম্ভব হয়নি") || lower.contains("sorry sir") || lower.contains("could not") -> "ERROR_FALLBACK"
            else -> null
        }
    }

    fun release() {
        stopRecording()
        stopPlayback()
    }
}
