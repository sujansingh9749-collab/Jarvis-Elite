package com.example.voice

import java.util.Locale

sealed class CommandIntent {
    data class OpenApp(val appName: String) : CommandIntent()
    object ToggleFlashlight : CommandIntent()
    object ToggleWifi : CommandIntent()
    object ToggleBluetooth : CommandIntent()
    data class VolumeControl(val increase: Boolean) : CommandIntent()
    data class MakeCall(val target: String) : CommandIntent()
    data class SendSms(val target: String, val message: String) : CommandIntent()
    data class SetAlarm(val hour: Int, val minute: Int, val label: String) : CommandIntent()
    object GetWeather : CommandIntent()
    object ScreenBack : CommandIntent()
    object ScreenHome : CommandIntent()
    object ScreenRecents : CommandIntent()
    object ScreenScrollDown : CommandIntent()
    object ScreenScrollUp : CommandIntent()
    object ScreenNotifications : CommandIntent()
    object ScreenQuickSettings : CommandIntent()
    object ScreenLock : CommandIntent()
    data class ScreenClickText(val textToClick: String) : CommandIntent()
    object ReadScreenText : CommandIntent()
    object RunDiagnostics : CommandIntent()
    object DailyBriefing : CommandIntent()
    data class ExecuteProtocol(val protocolName: String) : CommandIntent()
    data class SetSmartReminder(val note: String) : CommandIntent()
    data class LiveTranslate(val textToTranslate: String, val targetLanguage: String) : CommandIntent()
    data class CrossAppWorkflow(val workflowName: String, val appsList: List<String>, val actionsDescription: String) : CommandIntent()
    data class GeneralAIQuery(val prompt: String) : CommandIntent()
}

class IntentParser {

    fun parse(text: String, language: String): CommandIntent {
        val clean = text.lowercase(Locale.ROOT).trim()

        // Cross-App Workflow recognition (e.g., "open youtube and whatsapp", "multiple app", "cross app workflow", "commute workflow", "work workflow")
        if (clean.contains("cross app") || clean.contains("workflow") || clean.contains("ওয়ার্কফ্লো") || clean.contains("মাল্টি অ্যাপ") || clean.contains("multiple app") || (clean.contains("and ") && clean.contains("open ")) || (clean.contains("এবং ") && clean.contains("ওপেন"))) {
            when {
                clean.contains("commute") || clean.contains("drive") || clean.contains("ড্রাইভিং") || clean.contains("গাড়ি") -> {
                    return CommandIntent.CrossAppWorkflow(
                        workflowName = "Commute & Drive",
                        appsList = listOf("Maps", "YouTube", "Settings"),
                        actionsDescription = "Launching Navigation Maps + Media Player + Boosting System Audio"
                    )
                }
                clean.contains("work") || clean.contains("office") || clean.contains("অফিস") || clean.contains("কাজ") -> {
                    return CommandIntent.CrossAppWorkflow(
                        workflowName = "Work & Productivity",
                        appsList = listOf("WhatsApp", "Chrome", "Settings"),
                        actionsDescription = "Opening WhatsApp + Web Browser + Muting Volume for Focus"
                    )
                }
                clean.contains("social") || clean.contains("camera") || clean.contains("ক্যামেরা") -> {
                    return CommandIntent.CrossAppWorkflow(
                        workflowName = "Media Capture & Share",
                        appsList = listOf("Camera", "Gallery", "WhatsApp"),
                        actionsDescription = "Preparing Camera Capture + Photos Gallery + Instant WhatsApp Sharing"
                    )
                }
                else -> {
                    // Extract app names from user query (e.g. "open youtube and whatsapp", "ইউটিউব এবং হোয়াটসঅ্যাপ খোলো")
                    val apps = mutableListOf<String>()
                    if (clean.contains("youtube") || clean.contains("ইউটিউব")) apps.add("YouTube")
                    if (clean.contains("whatsapp") || clean.contains("হোয়াটসঅ্যাপ") || clean.contains("হোয়াটসঅ্যাপ")) apps.add("WhatsApp")
                    if (clean.contains("maps") || clean.contains("ম্যাপস")) apps.add("Maps")
                    if (clean.contains("camera") || clean.contains("ক্যামেরা")) apps.add("Camera")
                    if (clean.contains("facebook") || clean.contains("ফেইসবুক") || clean.contains("ফেসবুক")) apps.add("Facebook")
                    if (clean.contains("chrome") || clean.contains("ক্রোম")) apps.add("Chrome")

                    if (apps.isEmpty()) {
                        apps.add("YouTube")
                        apps.add("WhatsApp")
                    }

                    return CommandIntent.CrossAppWorkflow(
                        workflowName = "Chained Multi-App Task",
                        appsList = apps,
                        actionsDescription = "Executing multi-app sequence for ${apps.joinToString(", ")}"
                    )
                }
            }
        }

        // Live Translation (e.g., "translate hello to bengali", "অনুবাদ করো", "বাংলায় বলো", "english a translate করো", "transation")
        if (clean.contains("translate") || clean.contains("অনুবাদ") || clean.contains("ট্রান্সলেট") || clean.contains("বাংলায় বলো") || clean.contains("বাংলায় বল")) {
            val queryToTranslate = text.replace("translate", "", ignoreCase = true)
                .replace("অনুবাদ করো", "")
                .replace("ট্রান্সলেট করো", "")
                .replace("বাংলায় বলো", "")
                .trim()
            val targetLang = if (clean.contains("bengali") || clean.contains("বাংলা")) "BN" else "EN"
            return CommandIntent.LiveTranslate(queryToTranslate, targetLang)
        }

        // Daily Smart Briefing (e.g., "briefing", "brief me", "জার্ভিস ব্রিফিং", "রিপোর্ট দাও", "শুভ সকাল")
        if (clean.contains("briefing") || clean.contains("ব্রিফিং") || clean.contains("রিপোর্ট") || clean.contains("শুভ সকাল") || clean.contains("good morning")) {
            return CommandIntent.DailyBriefing
        }

        // Smart Protocol Automation (e.g., "Protocol Stealth", "Night Mode", "Defense Mode")
        if (clean.contains("protocol") || clean.contains("প্রটোকল") || clean.contains("stealth") || clean.contains("night mode") || clean.contains("defense mode") || clean.contains("আইরন ম্যান")) {
            val protocolName = when {
                clean.contains("stealth") || clean.contains("night") || clean.contains("নাইট") -> "Stealth Protocol"
                clean.contains("defense") || clean.contains("iron") || clean.contains("আইরন") -> "Defense Protocol"
                else -> "Clean Sweep Protocol"
            }
            return CommandIntent.ExecuteProtocol(protocolName)
        }

        // Smart Contextual Reminder
        if (clean.contains("reminder") || clean.contains("রিমাইন্ডার") || clean.contains("মনে করিয়ে দাও") || clean.contains("মনে করিয়ে")) {
            val note = clean.replace("reminder", "").replace("রিমাইন্ডার", "").replace("মনে করিয়ে দাও", "").replace("মনে করিয়ে দাও", "").trim()
            return CommandIntent.SetSmartReminder(if (note.isBlank()) "Important Task" else note)
        }

        // 1. Flashlight (Code-mixed: "torch", "flashlight", "টর্চ", "লাইটিং", "light on করো", "light জ্বালিয়ে দাও")
        if (clean.contains("torch") || clean.contains("flashlight") || clean.contains("টর্চ") || clean.contains("লাইটিং") || clean.contains("light on") || clean.contains("লাইটের")) {
            return CommandIntent.ToggleFlashlight
        }

        // 2. WiFi / Bluetooth
        if (clean.contains("wifi") || clean.contains("ওয়াইফাই") || clean.contains("ওয়াইফাই")) {
            return CommandIntent.ToggleWifi
        }
        if (clean.contains("bluetooth") || clean.contains("ব্লুটুথ")) {
            return CommandIntent.ToggleBluetooth
        }

        // 3. Volume (Code-mixed: "volume up", "sound up", "ভলিউম বাড়াও", "শব্দ বাড়াও", "volume বাড়িয়ে দাও", "vol plus")
        if (clean.contains("volume up") || clean.contains("sound up") || clean.contains("ভলিউম বাড়াও") || clean.contains("শব্দ বাড়াও") || clean.contains("volume বাড়িয়ে") || clean.contains("vol up")) {
            return CommandIntent.VolumeControl(increase = true)
        }
        if (clean.contains("volume down") || clean.contains("sound down") || clean.contains("ভলিউম কমাও") || clean.contains("শব্দ কমাও") || clean.contains("volume কমিয়ে") || clean.contains("vol down")) {
            return CommandIntent.VolumeControl(increase = false)
        }

        // 4. App Launching ("open youtube", "ইউটিউব খোলো", "whatsapp ওপেন করো", "facebook open করো")
        if (clean.contains("open ") || clean.contains("launch ") || clean.contains("খোলো") || clean.contains("খুলো") || clean.contains("ওপেন করো") || clean.contains("চালু করো")) {
            val appName = extractAppName(clean)
            if (appName.isNotBlank()) {
                return CommandIntent.OpenApp(appName)
            }
        }

        // 5. Calls & SMS ("call দাও", "phone করো", "call john")
        if (clean.contains("call ") || clean.contains("কল করো") || clean.contains("ফোন করো") || clean.contains("কল দাও")) {
            val target = clean.replace("call", "").replace("কল করো", "").replace("ফোন করো", "").replace("কল দাও", "").trim()
            return CommandIntent.MakeCall(if (target.isBlank()) "Contacts" else target)
        }
        if (clean.contains("message ") || clean.contains("sms ") || clean.contains("মেসেজ পাঠাও") || clean.contains("বার্তা পাঠাও")) {
            return CommandIntent.SendSms("Recipient", text)
        }

        // 6. Alarms
        if (clean.contains("alarm") || clean.contains("অ্যালার্ম")) {
            return CommandIntent.SetAlarm(7, 0, "Jarvis Alarm")
        }

        // 7. Weather (Code-mixed: "Jarvis, আমাকে weather টা বলো তো today-র", "weather", "আবহাওয়া", "আজকের weather")
        if (clean.contains("weather") || clean.contains("আবহাওয়া") || clean.contains("আবহাওয়া")) {
            return CommandIntent.GetWeather
        }

        // 8. Screen Navigation & Full Gesture Control Actions
        if (clean.contains("go back") || clean.contains("পেছনে যাও") || clean.contains("ব্যাক")) {
            return CommandIntent.ScreenBack
        }
        if (clean.contains("go home") || clean.contains("হোম স্ক্রিন") || clean.contains("হোমে যাও")) {
            return CommandIntent.ScreenHome
        }
        if (clean.contains("recent") || clean.contains("রিসেন্ট") || clean.contains("সাম্প্রতিক")) {
            return CommandIntent.ScreenRecents
        }
        if (clean.contains("notification") || clean.contains("নোটিফিকেশন")) {
            return CommandIntent.ScreenNotifications
        }
        if (clean.contains("quick settings") || clean.contains("কুইক সেটিংস")) {
            return CommandIntent.ScreenQuickSettings
        }
        if (clean.contains("lock screen") || clean.contains("স্ক্রিন লক") || clean.contains("ফোন লক")) {
            return CommandIntent.ScreenLock
        }
        if (clean.contains("scroll down") || clean.contains("নিচে নামো") || clean.contains("স্ক্রল ডাউন") || clean.contains("নিচে স্ক্রল")) {
            return CommandIntent.ScreenScrollDown
        }
        if (clean.contains("scroll up") || clean.contains("উপরে ওঠো") || clean.contains("স্ক্রল আপ") || clean.contains("উপরে স্ক্রল")) {
            return CommandIntent.ScreenScrollUp
        }
        if (clean.contains("read screen") || clean.contains("স্ক্রিনে কি আছে") || clean.contains("স্ক্রিন পড়ো") || clean.contains("স্ক্রিন পরো") || clean.contains("স্ক্রিন রিড")) {
            return CommandIntent.ReadScreenText
        }
        if (clean.contains("click ") || clean.contains("tap ") || clean.contains("ক্লিক করো") || clean.contains("ট্যাপ করো") || clean.contains("চাপ দাও")) {
            val target = clean.replace("click", "").replace("tap", "").replace("ক্লিক করো", "").replace("ট্যাপ করো", "").replace("চাপ দাও", "").trim()
            if (target.isNotBlank()) {
                return CommandIntent.ScreenClickText(target)
            }
        }

        // 9. Diagnostics
        if (clean.contains("diagnostic") || clean.contains("repair") || clean.contains("ডায়াগনস্টিক") || clean.contains("চেক করো")) {
            return CommandIntent.RunDiagnostics
        }

        // Default: AI Brain Query
        return CommandIntent.GeneralAIQuery(text)
    }

    private fun extractAppName(text: String): String {
        return text
            .replace("open ", "")
            .replace("launch ", "")
            .replace("খোলো", "")
            .replace("খুলো", "")
            .replace("ওপেন করো", "")
            .replace("চালু করো", "")
            .replace("অ্যাপ", "")
            .trim()
    }
}
