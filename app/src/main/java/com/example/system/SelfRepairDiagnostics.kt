package com.example.system

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
import com.example.data.repository.AIBrainRepository
import com.example.data.repository.MemoryRepository
import com.example.service.JarvisAccessibilityService

data class DiagnosticResult(
    val component: String,
    val status: String, // "HEALTHY", "WARNING", "ERROR"
    val detail: String,
    val repairAction: String? = null
)

class SelfRepairDiagnostics(
    private val context: Context,
    private val memoryRepo: MemoryRepository
) {

    suspend fun runFullDiagnostics(): List<DiagnosticResult> {
        val results = mutableListOf<DiagnosticResult>()

        // 1. Mic Permission Check
        val micPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (micPermission) {
            results.add(DiagnosticResult("Microphone Permission", "HEALTHY", "RECORD_AUDIO permission granted"))
        } else {
            results.add(DiagnosticResult("Microphone Permission", "ERROR", "RECORD_AUDIO permission missing", "Grant Microphone permission in Settings"))
        }

        // 2. Speech Recognizer Check
        val sttAvailable = SpeechRecognizer.isRecognitionAvailable(context)
        if (sttAvailable) {
            results.add(DiagnosticResult("Speech Engine (STT)", "HEALTHY", "Android SpeechRecognizer service available"))
        } else {
            results.add(DiagnosticResult("Speech Engine (STT)", "WARNING", "System SpeechRecognizer not available or disabled", "Install Google Speech Services"))
        }

        // 3. Gemini API Key Check
        val brainRepo = AIBrainRepository(context)
        val apiKey = brainRepo.getApiKey()
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            results.add(DiagnosticResult("Gemini AI API Key", "HEALTHY", "API Key configured successfully"))
        } else {
            results.add(DiagnosticResult("Gemini AI API Key", "WARNING", "API Key is missing or using placeholder", "Add key in Settings tab"))
        }

        // 4. Internet Connectivity
        val isConnected = checkInternet()
        if (isConnected) {
            results.add(DiagnosticResult("Network Connectivity", "HEALTHY", "Internet connection active (Online Gemini Mode)"))
        } else {
            results.add(DiagnosticResult("Network Connectivity", "WARNING", "Device is offline (Local Offline AI Engine Active)"))
        }

        // 5. Accessibility Screen Control Service
        val accessibilityActive = JarvisAccessibilityService.isServiceEnabled()
        if (accessibilityActive) {
            results.add(DiagnosticResult("Accessibility Service", "HEALTHY", "Screen control accessibility service active"))
        } else {
            results.add(DiagnosticResult("Accessibility Service", "WARNING", "Accessibility service disabled", "Enable J.A.R.V.I.S. in Accessibility Settings"))
        }

        // 6. Battery Level
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val batteryPct = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 100
        if (batteryPct > 15) {
            results.add(DiagnosticResult("Power & Battery", "HEALTHY", "Battery level at $batteryPct%"))
        } else {
            results.add(DiagnosticResult("Power & Battery", "WARNING", "Low battery ($batteryPct%). Power saving mode recommended"))
        }

        // Log results into DB
        memoryRepo.clearDiagnostics()
        for (res in results) {
            memoryRepo.logDiagnostic(res.component, res.status, "${res.detail} ${if (res.repairAction != null) "-> Action: ${res.repairAction}" else ""}")
        }

        return results
    }

    private fun checkInternet(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val net = cm.activeNetwork ?: return false
        val cap = cm.getNetworkCapabilities(net) ?: return false
        return cap.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
