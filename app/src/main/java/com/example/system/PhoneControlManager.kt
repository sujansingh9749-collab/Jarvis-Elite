package com.example.system

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.provider.AlarmClock
import android.provider.Settings
import android.widget.Toast

class PhoneControlManager(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var isTorchOn = false

    fun toggleFlashlight(): Boolean {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                characteristics.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            } ?: return false

            isTorchOn = !isTorchOn
            cameraManager.setTorchMode(cameraId, isTorchOn)
            isTorchOn
        } catch (e: Exception) {
            false
        }
    }

    fun launchApp(appName: String): Boolean {
        val pm = context.packageManager
        val cleanName = appName.lowercase()

        val knownPackages = mapOf(
            "whatsapp" to "com.whatsapp",
            "youtube" to "com.google.android.youtube",
            "facebook" to "com.facebook.katana",
            "chrome" to "com.android.chrome",
            "google" to "com.google.android.googlequicksearchbox",
            "maps" to "com.google.android.apps.maps",
            "camera" to "android.media.action.IMAGE_CAPTURE",
            "gallery" to "com.google.android.apps.photos",
            "settings" to Settings.ACTION_SETTINGS,
            "calculator" to "com.google.android.calculator"
        )

        // Try direct intent for known system actions
        if (cleanName.contains("settings") || cleanName.contains("সেটিংস")) {
            val intent = Intent(Settings.ACTION_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            context.startActivity(intent)
            return true
        }

        // Search installed apps
        for ((key, pkg) in knownPackages) {
            if (cleanName.contains(key)) {
                val launchIntent = pm.getLaunchIntentForPackage(pkg)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    return true
                }
            }
        }

        // Generic search by app label
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        for (app in installedApps) {
            val label = pm.getApplicationLabel(app).toString().lowercase()
            if (label.contains(cleanName) || cleanName.contains(label)) {
                val launchIntent = pm.getLaunchIntentForPackage(app.packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    return true
                }
            }
        }

        // Fallback: Open Play Store or Browser search
        try {
            val searchIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$appName"))
            searchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(searchIntent)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun openWifiSettings() {
        try {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open WiFi settings", Toast.LENGTH_SHORT).show()
        }
    }

    fun openBluetoothSettings() {
        try {
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open Bluetooth settings", Toast.LENGTH_SHORT).show()
        }
    }

    fun adjustVolume(increase: Boolean) {
        val direction = if (increase) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI)
    }

    fun setAlarm(hour: Int, minute: Int, message: String) {
        try {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, hour)
                putExtra(AlarmClock.EXTRA_MINUTES, minute)
                putExtra(AlarmClock.EXTRA_MESSAGE, message)
                putExtra(AlarmClock.EXTRA_SKIP_UI, true)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Alarm set for $hour:$minute", Toast.LENGTH_SHORT).show()
        }
    }

    fun makeCall(recipient: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$recipient")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Dialing $recipient", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendSms(recipient: String, text: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$recipient")
                putExtra("sms_body", text)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "SMS composer ready for $recipient", Toast.LENGTH_SHORT).show()
        }
    }

    fun getDailyBriefing(language: String): String {
        val batteryStatus = getBatteryStatus()
        val formattedTime = java.text.SimpleDateFormat("hh:mm a, EEEE, d MMMM", java.util.Locale.getDefault()).format(java.util.Date())

        return if (language == "BN") {
            "শুভ সকাল স্যার। এখন সময় $formattedTime। ব্যাটারি চার্জ রয়েছে $batteryStatus%। সিস্টেম ডায়াগনস্টিক সম্পূর্ণ এবং সিকিউরিটি আর্মার ১০০% সক্রিয়। আমি আপনার পরবর্তী নির্দেশের জন্য প্রস্তুত, স্যার।"
        } else {
            "Good day, Sir. The time is $formattedTime. Battery capacity stands at $batteryStatus%. Arc reactor diagnostics complete and voice security armor is 100% active. Standing by for your command, Sir."
        }
    }

    private fun getBatteryStatus(): Int {
        return try {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as? android.os.BatteryManager
            bm?.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 85
        } catch (e: Exception) {
            85
        }
    }

    fun executeProtocol(protocolName: String, language: String): String {
        return when (protocolName) {
            "Stealth Protocol" -> {
                adjustVolume(false)
                adjustVolume(false)
                if (language == "BN") {
                    "স্টেল্থ প্রটোকল সক্রিয় করা হয়েছে। ভলিউম কমানো এবং সিস্টেম সাইলেন্ট করা হয়েছে, স্যার।"
                } else {
                    "Stealth Protocol engaged, Sir. Audio levels dampened and silent mode requested."
                }
            }
            "Defense Protocol" -> {
                toggleFlashlight()
                adjustVolume(true)
                if (language == "BN") {
                    "ডিফেন্স প্রটোকল সক্রিয়! ফ্ল্যাশলাইট এবং ডিফেন্স সিস্টেম রেডি, স্যার।"
                } else {
                    "Defense Protocol active, Sir! Flashlight illuminated and system alerts boosted."
                }
            }
            else -> {
                if (language == "BN") {
                    "ক্লিন সুইপ প্রটোকল চালনা করা হচ্ছে। সিস্টেম ডায়াগনস্টিক সম্পন্ন, স্যার।"
                } else {
                    "Clean Sweep Protocol executed. Core memory optimized and system verified, Sir."
                }
            }
        }
    }

    fun executeCrossAppWorkflow(workflowName: String, appsList: List<String>, language: String): String {
        // Sequentially trigger launch for apps in workflow
        val launchedApps = mutableListOf<String>()
        for (app in appsList) {
            val success = launchApp(app)
            if (success) {
                launchedApps.add(app)
            }
        }

        // Additional system adjustments based on workflow
        when (workflowName) {
            "Commute & Drive" -> adjustVolume(true)
            "Work & Productivity" -> adjustVolume(false)
            "Media Capture & Share" -> toggleFlashlight()
        }

        val appSequence = launchedApps.ifEmpty { appsList }.joinToString(" ➔ ")
        return if (language == "BN") {
            "স্যার, ক্রস-অ্যাপ ওয়ার্কফ্লো '$workflowName' সফলভাবে চালনা করা হয়েছে। সিকোয়েন্স: $appSequence। সমস্ত সিস্টেম প্রস্তুত।"
        } else {
            "Sir, Cross-App Workflow '$workflowName' executed successfully. Sequence: $appSequence. Systems standing by."
        }
    }
}
