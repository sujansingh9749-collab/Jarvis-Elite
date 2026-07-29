package com.example

import com.example.data.repository.AIResponse
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.data.db.JarvisDatabase
import com.example.data.repository.AIBrainRepository
import com.example.data.repository.MemoryRepository
import com.example.service.JarvisAccessibilityService
import com.example.service.JarvisForegroundService
import com.example.service.JarvisNotificationListenerService
import com.example.system.DiagnosticResult
import com.example.system.PhoneControlManager
import com.example.system.SelfRepairDiagnostics
import com.example.ui.components.ArcReactorState
import com.example.ui.components.JarvisBottomNav
import com.example.ui.components.JarvisTab
import com.example.ui.screens.DiagnosticsScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HudScreen
import com.example.ui.screens.MemoryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.voice.CommandIntent
import com.example.voice.IntentParser
import com.example.voice.SpeechManager
import com.example.voice.TtsManager
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var database: JarvisDatabase
    private lateinit var memoryRepo: MemoryRepository
    private lateinit var aiBrainRepo: AIBrainRepository
    private lateinit var phoneControl: PhoneControlManager
    private lateinit var diagnostics: SelfRepairDiagnostics
    private lateinit var voiceMatchManager: com.example.voice.VoiceMatchManager
    private val intentParser = IntentParser()

    private var speechManager: SpeechManager? = null
    private var ttsManager: TtsManager? = null

    // UI States
    private var currentTab by mutableStateOf<JarvisTab>(JarvisTab.HUD)
    private var currentLanguage by mutableStateOf("BN") // Default Bangla
    private var arcState by mutableStateOf(ArcReactorState.IDLE)
    private var speechTranscript by mutableStateOf("")
    private var jarvisResponse by mutableStateOf("Sir, J.A.R.V.I.S. online and ready. \"জার্ভিস\" বলুন।")
    private var isOnlineMode by mutableStateOf(true)
    private var volumeRms by mutableFloatStateOf(0f)
    private var diagnosticResults by mutableStateOf<List<DiagnosticResult>>(emptyList())
    private var isDiagRunning by mutableStateOf(false)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val micGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        if (micGranted) {
            initVoicePipeline()
        } else {
            Toast.makeText(this, "Microphone permission required for J.A.R.V.I.S.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        database = JarvisDatabase.getInstance(applicationContext)
        memoryRepo = MemoryRepository(database.jarvisDao())
        aiBrainRepo = AIBrainRepository(applicationContext)
        phoneControl = PhoneControlManager(applicationContext)
        diagnostics = SelfRepairDiagnostics(applicationContext, memoryRepo)
        voiceMatchManager = com.example.voice.VoiceMatchManager(applicationContext)

        // Check & request permissions
        checkAndRequestPermissions()

        // Init TTS Engine
        ttsManager = TtsManager(
            context = applicationContext,
            onSpeechStart = { arcState = ArcReactorState.SPEAKING },
            onSpeechDone = { arcState = ArcReactorState.IDLE }
        )

        // Start Foreground Service
        JarvisForegroundService.startService(applicationContext)

        setContent {
            MyApplicationTheme {
                val scope = rememberCoroutineScope()

                val conversations by memoryRepo.conversations.collectAsState(initial = emptyList())
                val memoryFacts by memoryRepo.memoryFacts.collectAsState(initial = emptyList())
                val reminders by memoryRepo.reminders.collectAsState(initial = emptyList())

                Scaffold(
                    bottomBar = {
                        JarvisBottomNav(
                            currentRoute = currentTab.route,
                            onTabSelected = { currentTab = it }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            JarvisTab.HUD -> HudScreen(
                                currentLanguage = currentLanguage,
                                arcState = arcState,
                                speechTranscript = speechTranscript,
                                jarvisResponse = jarvisResponse,
                                isOnline = isOnlineMode,
                                volumeRms = volumeRms,
                                onLanguageToggle = {
                                    currentLanguage = if (currentLanguage == "BN") "EN" else "BN"
                                    val langName = if (currentLanguage == "BN") "বাংলা" else "English"
                                    speakAndRespond("Language set to $langName, Sir.")
                                },
                                onMicClick = { triggerListening() },
                                onQuickAction = { action ->
                                    handleQuickAction(action, scope)
                                }
                            )

                            JarvisTab.History -> HistoryScreen(
                                conversations = conversations,
                                onSpeakText = { text, lang ->
                                    ttsManager?.speak(text, lang)
                                },
                                onDeleteConversation = { id ->
                                    scope.launch { memoryRepo.deleteConversation(id) }
                                },
                                onClearAll = {
                                    scope.launch { memoryRepo.clearHistory() }
                                }
                            )

                            JarvisTab.Memory -> MemoryScreen(
                                memoryFacts = memoryFacts,
                                reminders = reminders,
                                onAddFact = { key, value ->
                                    scope.launch { memoryRepo.saveFact(key, value) }
                                },
                                onDeleteFact = { id ->
                                    scope.launch { memoryRepo.deleteFact(id) }
                                },
                                onAddReminder = { title, time ->
                                    scope.launch { memoryRepo.addReminder(title, time) }
                                },
                                onToggleReminder = { id, done ->
                                    scope.launch { memoryRepo.toggleReminder(id, done) }
                                },
                                onDeleteReminder = { id ->
                                    scope.launch { memoryRepo.deleteReminder(id) }
                                }
                            )

                            JarvisTab.Diagnostics -> DiagnosticsScreen(
                                diagnosticResults = diagnosticResults,
                                isRunning = isDiagRunning,
                                onRunDiagnostics = {
                                    scope.launch {
                                        isDiagRunning = true
                                        diagnosticResults = diagnostics.runFullDiagnostics()
                                        isDiagRunning = false
                                    }
                                }
                            )

                            JarvisTab.Settings -> {
                                val prefs = getSharedPreferences("jarvis_settings", Context.MODE_PRIVATE)
                                val presetId = prefs.getString("voice_preset_id", com.example.voice.VoiceConstants.PRESET_JARVIS_CLASSIC.id) ?: com.example.voice.VoiceConstants.PRESET_JARVIS_CLASSIC.id
                                val rate = prefs.getFloat("speech_rate", 1.0f)
                                val pitch = prefs.getFloat("speech_pitch", 1.0f)
                                val enableClips = prefs.getBoolean("enable_custom_clips", true)
                                val systemVoiceName = prefs.getString("system_voice_name", null)
                                val availableVoices = ttsManager?.getAvailableSystemVoices() ?: emptyList()
                                val cvManager = ttsManager?.customVoiceManager ?: com.example.voice.CustomVoiceManager(applicationContext)

                                SettingsScreen(
                                    currentApiKey = aiBrainRepo.getApiKey(),
                                    speechRate = rate,
                                    speechPitch = pitch,
                                    currentPresetId = presetId,
                                    enableCustomClips = enableClips,
                                    availableSystemVoices = availableVoices,
                                    selectedSystemVoiceName = systemVoiceName,
                                    customVoiceManager = cvManager,
                                    voiceMatchManager = voiceMatchManager,
                                    speechManager = speechManager,
                                    currentLanguage = currentLanguage,
                                    isForegroundServiceActive = true,
                                    onSaveApiKey = { key ->
                                        prefs.edit().putString("custom_api_key", key).apply()
                                        Toast.makeText(applicationContext, "API Key saved successfully", Toast.LENGTH_SHORT).show()
                                    },
                                    onUpdateVoiceParams = { newRate, newPitch ->
                                        prefs.edit()
                                            .putFloat("speech_rate", newRate)
                                            .putFloat("speech_pitch", newPitch)
                                            .putString("voice_preset_id", com.example.voice.VoiceConstants.PRESET_CUSTOM.id)
                                            .apply()
                                        ttsManager?.setVoiceParameters()
                                    },
                                    onSelectPreset = { preset ->
                                        prefs.edit()
                                            .putString("voice_preset_id", preset.id)
                                            .putFloat("speech_rate", preset.speechRate)
                                            .putFloat("speech_pitch", preset.pitch)
                                            .apply()
                                        ttsManager?.setVoiceParameters()
                                    },
                                    onSelectSystemVoice = { name ->
                                        if (name != null) {
                                            prefs.edit().putString("system_voice_name", name).apply()
                                        } else {
                                            prefs.edit().remove("system_voice_name").apply()
                                        }
                                        ttsManager?.setVoiceParameters()
                                    },
                                    onToggleCustomClips = { enable ->
                                        prefs.edit().putBoolean("enable_custom_clips", enable).apply()
                                    },
                                    onPreviewPreset = { preset ->
                                        ttsManager?.previewPreset(preset, currentLanguage)
                                    },
                                    onPreviewClip = { key ->
                                        ttsManager?.previewCustomClip(key)
                                    },
                                    onToggleForegroundService = { enable ->
                                        if (enable) {
                                            JarvisForegroundService.startService(applicationContext)
                                        } else {
                                            JarvisForegroundService.stopService(applicationContext)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Process Deep Link / App Action intent on initial launch
        handleDeepLink(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val data = intent?.data ?: return
        val scheme = data.scheme?.lowercase() ?: return
        if (scheme != "jarvis" && scheme != "http" && scheme != "https") return

        val host = data.host?.lowercase() ?: ""
        val path = data.path?.lowercase() ?: ""

        // 1. Navigation routing (e.g., jarvis://navigate?tab=diagnostics or https://jarvis.ai/open?tab=settings)
        val tabParam = data.getQueryParameter("tab")?.lowercase()
        if (host == "navigate" || path == "/navigate" || path == "/open" || tabParam != null) {
            when (tabParam ?: host) {
                "hud" -> currentTab = JarvisTab.HUD
                "history" -> currentTab = JarvisTab.History
                "memory" -> currentTab = JarvisTab.Memory
                "diagnostics" -> currentTab = JarvisTab.Diagnostics
                "settings" -> currentTab = JarvisTab.Settings
            }
        }

        // 2. Control actions (e.g., jarvis://control?action=listen or jarvis://control?action=briefing)
        val actionParam = data.getQueryParameter("action")?.lowercase()
        if (host == "control" || path == "/control" || actionParam != null) {
            when (actionParam) {
                "listen" -> triggerListening()
                "briefing" -> {
                    val reply = phoneControl.getDailyBriefing(currentLanguage)
                    speakAndRespond(reply)
                }
                "flashlight" -> {
                    val isOn = phoneControl.toggleFlashlight()
                    val reply = if (isOn) "Flashlight ON" else "Flashlight OFF"
                    speakAndRespond(reply)
                }
                "vol_up" -> {
                    phoneControl.adjustVolume(true)
                    speakAndRespond("Volume Up")
                }
                "vol_down" -> {
                    phoneControl.adjustVolume(false)
                    speakAndRespond("Volume Down")
                }
                "diagnostics" -> {
                    currentTab = JarvisTab.Diagnostics
                }
            }
        }

        // 3. Direct Query execution (e.g., jarvis://query?text=turn+on+flashlight or https://jarvis.ai/query?text=what+is+the+weather)
        val textQuery = data.getQueryParameter("text") ?: data.getQueryParameter("q")
        if (!textQuery.isNullOrBlank()) {
            speechTranscript = textQuery
            processUserSpeech(textQuery)
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            initVoicePipeline()
        }
    }

    private fun initVoicePipeline() {
        speechManager = SpeechManager(
            context = applicationContext,
            onResult = { resultText ->
                speechTranscript = resultText
                processUserSpeech(resultText)
            },
            onError = { errMsg ->
                if (arcState == ArcReactorState.LISTENING) {
                    arcState = ArcReactorState.IDLE
                }
            },
            onListeningState = { listening ->
                arcState = if (listening) ArcReactorState.LISTENING else ArcReactorState.IDLE
            },
            onVolumeChanged = { rms -> volumeRms = rms }
        )
    }

    private fun triggerListening() {
        if (arcState == ArcReactorState.SPEAKING) {
            ttsManager?.stop()
        }
        arcState = ArcReactorState.LISTENING
        speechManager?.startListening(currentLanguage)
    }

    private fun processUserSpeech(userSpeech: String) {
        arcState = ArcReactorState.PROCESSING

        // Biometric Voice Match Security Check
        if (voiceMatchManager.isLockEnabled) {
            val capturedRms = speechManager?.getCapturedRmsList() ?: emptyList()
            val verification = voiceMatchManager.verifySpeaker(capturedRms, userSpeech)

            if (!verification.isVerified) {
                val securityReply = if (currentLanguage == "BN") {
                    "অ্যাক্সেস প্রত্যাখ্যান করা হয়েছে, স্যার। আপনার ভয়েস প্রিন্ট সিগনেচার মিলেনি (${verification.confidencePercent}%)।"
                } else {
                    "Access Denied, Sir. Voice frequency signature mismatch (${verification.confidencePercent}% match)."
                }
                speakAndRespond(securityReply)
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                    memoryRepo.saveConversation(userSpeech, securityReply, "SECURITY_DENIED", currentLanguage)
                }
                return
            }
        }

        val parsedIntent = intentParser.parse(userSpeech, currentLanguage)

        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            when (parsedIntent) {
                is CommandIntent.ToggleFlashlight -> {
                    val isOn = phoneControl.toggleFlashlight()
                    val reply = if (currentLanguage == "BN") {
                        if (isOn) "স্যার, ফ্ল্যাশলাইট চালু করা হয়েছে।" else "স্যার, ফ্ল্যাশলাইট বন্ধ করা হয়েছে।"
                    } else {
                        if (isOn) "Flashlight activated, Sir." else "Flashlight deactivated, Sir."
                    }
                    speakAndRespond(reply)
                    memoryRepo.saveConversation(userSpeech, reply, "LOCAL_ACTION", currentLanguage)
                }

                is CommandIntent.OpenApp -> {
                    val appName = parsedIntent.appName
                    val launched = phoneControl.launchApp(appName)
                    val reply = if (launched) {
                        if (currentLanguage == "BN") "$appName অ্যাপ চালু করা হচ্ছে, স্যার।" else "Opening $appName, Sir."
                    } else {
                        if (currentLanguage == "BN") "দুঃখিত স্যার, $appName খুঁজে পাওয়া যায়নি।" else "Sorry Sir, could not locate $appName."
                    }
                    speakAndRespond(reply)
                    memoryRepo.saveConversation(userSpeech, reply, "LOCAL_ACTION", currentLanguage)
                }

                is CommandIntent.ToggleWifi -> {
                    phoneControl.openWifiSettings()
                    val reply = if (currentLanguage == "BN") "ওয়াইফাই সেটিংস খোলা হচ্ছে, স্যার।" else "Opening WiFi settings, Sir."
                    speakAndRespond(reply)
                    memoryRepo.saveConversation(userSpeech, reply, "LOCAL_ACTION", currentLanguage)
                }

                is CommandIntent.ToggleBluetooth -> {
                    phoneControl.openBluetoothSettings()
                    val reply = if (currentLanguage == "BN") "ব্লুটুথ সেটিংস খোলা হচ্ছে, স্যার।" else "Opening Bluetooth settings, Sir."
                    speakAndRespond(reply)
                    memoryRepo.saveConversation(userSpeech, reply, "LOCAL_ACTION", currentLanguage)
                }

                is CommandIntent.VolumeControl -> {
                    phoneControl.adjustVolume(parsedIntent.increase)
                    val reply = if (currentLanguage == "BN") {
                        if (parsedIntent.increase) "ভলিউম বাড়ানো হয়েছে, স্যার।" else "ভলিউম কমানো হয়েছে, স্যার।"
                    } else {
                        if (parsedIntent.increase) "Volume increased, Sir." else "Volume decreased, Sir."
                    }
                    speakAndRespond(reply)
                    memoryRepo.saveConversation(userSpeech, reply, "LOCAL_ACTION", currentLanguage)
                }

                is CommandIntent.MakeCall -> {
                    phoneControl.makeCall(parsedIntent.target)
                    val reply = if (currentLanguage == "BN") "${parsedIntent.target} কে কল দেওয়া হচ্ছে..." else "Initiating call to ${parsedIntent.target}, Sir."
                    speakAndRespond(reply)
                    memoryRepo.saveConversation(userSpeech, reply, "LOCAL_ACTION", currentLanguage)
                }

                is CommandIntent.SendSms -> {
                    phoneControl.sendSms(parsedIntent.target, parsedIntent.message)
                    val reply = if (currentLanguage == "BN") "মেসেজ কম্পোজার খোলা হয়েছে, স্যার।" else "Opening SMS composer, Sir."
                    speakAndRespond(reply)
                    memoryRepo.saveConversation(userSpeech, reply, "LOCAL_ACTION", currentLanguage)
                }

                is CommandIntent.SetAlarm -> {
                    phoneControl.setAlarm(parsedIntent.hour, parsedIntent.minute, parsedIntent.label)
                    val reply = if (currentLanguage == "BN") "অ্যালার্ম সেট করা হয়েছে, স্যার।" else "Alarm set, Sir."
                    speakAndRespond(reply)
                    memoryRepo.saveConversation(userSpeech, reply, "LOCAL_ACTION", currentLanguage)
                }

                is CommandIntent.GetWeather -> {
                    val prompt = "User asked about weather in code-mixed language (Banglish/English/Bengali): '$userSpeech'. Give a crisp, stylish Jarvis weather report with temperature and sky condition."
                    val aiResult = aiBrainRepo.generateResponse(prompt, currentLanguage)
                    val reply = when (aiResult) {
                        is AIResponse.Success -> aiResult.text
                        is AIResponse.Error -> if (currentLanguage == "BN") "স্যার, বর্তমান তাপমাত্রা ২৮° সে., আকাশ পরিষ্কার।" else "Atmospheric temperature is 28°C with fair skies, Sir."
                    }
                    speakAndRespond(reply)
                    memoryRepo.saveConversation(userSpeech, reply, "WEATHER_REPORT", currentLanguage)
                }

                is CommandIntent.LiveTranslate -> {
                    val targetLang = parsedIntent.targetLanguage
                    val translatePrompt = "Live Translation Request: Translate the following into ${if (targetLang == "BN") "fluent, natural Bengali" else "English"}. Give direct translated output with polite Jarvis tone. Text: '${parsedIntent.textToTranslate}'"
                    val aiResult = aiBrainRepo.generateResponse(translatePrompt, targetLang)
                    val reply = when (aiResult) {
                        is AIResponse.Success -> aiResult.text
                        is AIResponse.Error -> aiResult.fallbackText
                    }
                    speakAndRespond(reply)
                    memoryRepo.saveConversation(userSpeech, reply, "LIVE_TRANSLATION", targetLang)
                }

                is CommandIntent.CrossAppWorkflow -> {
                    val reply = phoneControl.executeCrossAppWorkflow(
                        parsedIntent.workflowName,
                        parsedIntent.appsList,
                        currentLanguage
                    )
                    speakAndRespond(reply)
                    memoryRepo.saveConversation(userSpeech, reply, "CROSS_APP_WORKFLOW", currentLanguage)
                }

                is CommandIntent.DailyBriefing -> {
                    val reply = phoneControl.getDailyBriefing(currentLanguage)
                    speakAndRespond(reply)
                    memoryRepo.saveConversation(userSpeech, reply, "DAILY_BRIEFING", currentLanguage)
                }

                is CommandIntent.ScreenBack -> {
                    JarvisAccessibilityService.performBack()
                    val reply = if (currentLanguage == "BN") "পেছনে যাওয়া হচ্ছে, স্যার।" else "Going back, Sir."
                    speakAndRespond(reply)
                }

                is CommandIntent.ScreenHome -> {
                    JarvisAccessibilityService.performHome()
                    val reply = if (currentLanguage == "BN") "হোম স্ক্রিনে যাওয়া হচ্ছে, স্যার।" else "Navigating home, Sir."
                    speakAndRespond(reply)
                }

                is CommandIntent.ScreenRecents -> {
                    JarvisAccessibilityService.performRecents()
                    val reply = if (currentLanguage == "BN") "সাম্প্রতিক অ্যাপস সমূহের তালিকা খোলা হচ্ছে, স্যার।" else "Opening recent tasks, Sir."
                    speakAndRespond(reply)
                }

                is CommandIntent.ScreenNotifications -> {
                    JarvisAccessibilityService.openNotifications()
                    val reply = if (currentLanguage == "BN") "নোটিফিকেশন প্যানেল নামানো হচ্ছে, স্যার।" else "Opening notification shade, Sir."
                    speakAndRespond(reply)
                }

                is CommandIntent.ScreenQuickSettings -> {
                    JarvisAccessibilityService.openQuickSettings()
                    val reply = if (currentLanguage == "BN") "কুইক সেটিংস খোলা হচ্ছে, স্যার।" else "Opening Quick Settings, Sir."
                    speakAndRespond(reply)
                }

                is CommandIntent.ScreenLock -> {
                    JarvisAccessibilityService.lockScreen()
                    val reply = if (currentLanguage == "BN") "স্ক্রিন লক করা হয়েছে, স্যার।" else "Screen locked, Sir."
                    speakAndRespond(reply)
                }

                is CommandIntent.ScreenScrollDown -> {
                    JarvisAccessibilityService.scrollDown()
                    val reply = if (currentLanguage == "BN") "নিচে স্ক্রল করা হচ্ছে, স্যার।" else "Scrolling down, Sir."
                    speakAndRespond(reply)
                }

                is CommandIntent.ScreenScrollUp -> {
                    JarvisAccessibilityService.scrollUp()
                    val reply = if (currentLanguage == "BN") "উপরে স্ক্রল করা হচ্ছে, স্যার।" else "Scrolling up, Sir."
                    speakAndRespond(reply)
                }

                is CommandIntent.ScreenClickText -> {
                    val clicked = JarvisAccessibilityService.clickTextOnScreen(parsedIntent.textToClick)
                    val reply = if (clicked) {
                        if (currentLanguage == "BN") "'${parsedIntent.textToClick}' এ ক্লিক করা হয়েছে, স্যার।" else "Clicked '${parsedIntent.textToClick}', Sir."
                    } else {
                        if (currentLanguage == "BN") "স্যার, স্ক্রিনে '${parsedIntent.textToClick}' খুঁজে পাওয়া যায়নি।" else "Could not find '${parsedIntent.textToClick}' on current screen, Sir."
                    }
                    speakAndRespond(reply)
                }

                is CommandIntent.ReadScreenText -> {
                    val screenContent = JarvisAccessibilityService.readActiveScreenText()
                    val summaryPrompt = "Read and summarize this screen content for the user: $screenContent"
                    val aiResult = aiBrainRepo.generateResponse(summaryPrompt, currentLanguage)
                    val reply = when (aiResult) {
                        is com.example.data.repository.AIResponse.Success -> aiResult.text
                        is com.example.data.repository.AIResponse.Error -> screenContent.take(200)
                    }
                    speakAndRespond(reply)
                }

                is CommandIntent.RunDiagnostics -> {
                    currentTab = JarvisTab.Diagnostics
                    val results = diagnostics.runFullDiagnostics()
                    diagnosticResults = results
                    val reply = if (currentLanguage == "BN") "ডায়াগনস্টিক রিপোর্ট সম্পূর্ণ হয়েছে, স্যার।" else "Diagnostic report complete, Sir."
                    speakAndRespond(reply)
                }

                is CommandIntent.ExecuteProtocol -> {
                    val reply = phoneControl.executeProtocol(parsedIntent.protocolName, currentLanguage)
                    speakAndRespond(reply)
                    memoryRepo.saveConversation(userSpeech, reply, "SMART_PROTOCOL", currentLanguage)
                }

                is CommandIntent.SetSmartReminder -> {
                    phoneControl.setAlarm(8, 0, parsedIntent.note)
                    val reply = if (currentLanguage == "BN") {
                        "স্যার, '${parsedIntent.note}' এর জন্য রিমাইন্ডার এবং অ্যালার্ম শিডিউল করা হয়েছে।"
                    } else {
                        "Smart reminder and alarm set for '${parsedIntent.note}', Sir."
                    }
                    speakAndRespond(reply)
                    memoryRepo.saveConversation(userSpeech, reply, "SMART_REMINDER", currentLanguage)
                }

                is CommandIntent.GeneralAIQuery -> {
                    val aiResult = aiBrainRepo.generateResponse(userSpeech, currentLanguage)
                    when (aiResult) {
                        is com.example.data.repository.AIResponse.Success -> {
                            isOnlineMode = aiResult.isOnline
                            speakAndRespond(aiResult.text)
                            memoryRepo.saveConversation(
                                userSpeech,
                                aiResult.text,
                                if (aiResult.isOnline) "GEMINI_ONLINE" else "LOCAL_AI",
                                currentLanguage
                            )
                        }
                        is com.example.data.repository.AIResponse.Error -> {
                            speakAndRespond(aiResult.fallbackText)
                        }
                    }
                }
            }
        }
    }

    private fun handleQuickAction(action: String, scope: kotlinx.coroutines.CoroutineScope) {
        when (action) {
            "flashlight" -> {
                val isOn = phoneControl.toggleFlashlight()
                val reply = if (isOn) "Flashlight ON" else "Flashlight OFF"
                speakAndRespond(reply)
            }
            "vol_up" -> {
                phoneControl.adjustVolume(true)
                speakAndRespond("Volume Up")
            }
            "vol_down" -> {
                phoneControl.adjustVolume(false)
                speakAndRespond("Volume Down")
            }
            "wifi" -> phoneControl.openWifiSettings()
            "home" -> JarvisAccessibilityService.performHome()
            "replay_tts" -> ttsManager?.speak(jarvisResponse, currentLanguage)
            "test_code_mix" -> {
                val query = "Jarvis, আমাকে weather টা বলো তো today-র"
                speechTranscript = query
                processUserSpeech(query)
            }
            "test_translation" -> {
                val query = "Translate 'Good morning Sir, all suit systems are fully operational' to Bengali"
                speechTranscript = query
                processUserSpeech(query)
            }
            "workflow_commute" -> {
                val query = "Jarvis, start commute workflow with Maps and YouTube"
                speechTranscript = query
                processUserSpeech(query)
            }
            "workflow_work" -> {
                val query = "Jarvis, run work workflow with WhatsApp and Chrome"
                speechTranscript = query
                processUserSpeech(query)
            }
            "workflow_social" -> {
                val query = "Jarvis, start social workflow with Camera, Gallery and WhatsApp"
                speechTranscript = query
                processUserSpeech(query)
            }
        }
    }

    private fun speakAndRespond(text: String) {
        jarvisResponse = text
        arcState = ArcReactorState.SPEAKING
        ttsManager?.speak(text, currentLanguage)
    }

    override fun onDestroy() {
        super.onDestroy()
        speechManager?.destroy()
        ttsManager?.shutdown()
    }
}
