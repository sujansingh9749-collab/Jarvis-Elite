package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.speech.tts.Voice
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.JarvisAccessibilityService
import com.example.ui.components.VoiceCustomizationSection
import com.example.ui.components.VoiceMatchSection
import com.example.ui.theme.ArcOrange
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DeepSpaceDark
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.TextMutedCyan
import com.example.ui.theme.TextPrimaryCyan
import com.example.ui.theme.TextSecondaryCyan
import com.example.voice.CustomVoiceManager
import com.example.voice.SpeechManager
import com.example.voice.VoiceMatchManager
import com.example.voice.VoicePreset

@Composable
fun SettingsScreen(
    currentApiKey: String,
    speechRate: Float,
    speechPitch: Float,
    currentPresetId: String,
    enableCustomClips: Boolean,
    availableSystemVoices: List<Voice>,
    selectedSystemVoiceName: String?,
    customVoiceManager: CustomVoiceManager,
    voiceMatchManager: VoiceMatchManager,
    speechManager: SpeechManager?,
    currentLanguage: String,
    isForegroundServiceActive: Boolean,
    onSaveApiKey: (String) -> Unit,
    onUpdateVoiceParams: (Float, Float) -> Unit,
    onSelectPreset: (VoicePreset) -> Unit,
    onSelectSystemVoice: (String?) -> Unit,
    onToggleCustomClips: (Boolean) -> Unit,
    onPreviewPreset: (VoicePreset) -> Unit,
    onPreviewClip: (String) -> Unit,
    onToggleForegroundService: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var apiKeyInput by remember { mutableStateOf(currentApiKey) }
    var foregroundActive by remember { mutableStateOf(isForegroundServiceActive) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpaceDark)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(text = "JARVIS CONFIGURATION", style = MaterialTheme.typography.titleMedium, color = CyanPrimary, fontWeight = FontWeight.Bold)
        Text(text = "API keys, voice pipeline, and background services", style = MaterialTheme.typography.labelSmall, color = TextMutedCyan)

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Gemini API Key Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurfaceCardBorder, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Key, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.padding(start = 8.dp))
                    Text(text = "Gemini AI API Key (Online Brain)", style = MaterialTheme.typography.titleSmall, color = TextPrimaryCyan, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    label = { Text("API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = SurfaceCardBorder,
                        focusedTextColor = TextPrimaryCyan,
                        unfocusedTextColor = TextPrimaryCyan
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onSaveApiKey(apiKeyInput.trim()) },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Save Key", color = DeepSpaceDark, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Comprehensive Voice Engine Customization & Custom Clips
        VoiceCustomizationSection(
            currentPresetId = currentPresetId,
            currentRate = speechRate,
            currentPitch = speechPitch,
            enableCustomClips = enableCustomClips,
            availableSystemVoices = availableSystemVoices,
            selectedSystemVoiceName = selectedSystemVoiceName,
            customVoiceManager = customVoiceManager,
            onSelectPreset = onSelectPreset,
            onUpdateVoiceParams = onUpdateVoiceParams,
            onSelectSystemVoice = onSelectSystemVoice,
            onToggleCustomClips = onToggleCustomClips,
            onPreviewPreset = onPreviewPreset,
            onPreviewClip = onPreviewClip
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Biometric Voice Match Owner Access Control
        VoiceMatchSection(
            voiceMatchManager = voiceMatchManager,
            speechManager = speechManager,
            currentLanguage = currentLanguage,
            onLockToggled = { enabled ->
                Toast.makeText(
                    context,
                    if (enabled) "Voice Match Lock Activated! JARVIS responds only to owner voice." else "Voice Match Lock Disabled",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Offline Local LLM Status Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurfaceCardBorder, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.DownloadDone, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.padding(start = 12.dp))
                Column {
                    Text(text = "Offline Local LLM Engine", style = MaterialTheme.typography.titleSmall, color = TextPrimaryCyan, fontWeight = FontWeight.Bold)
                    Text(text = "Embedded rule parser & fallback offline LLM ready for zero-latency responses.", style = MaterialTheme.typography.bodySmall, color = TextSecondaryCyan)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Foreground Service Toggle
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurfaceCardBorder, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(14.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = ArcOrange, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.padding(start = 10.dp))
                    Column {
                        Text(text = "24/7 Foreground Service", style = MaterialTheme.typography.titleSmall, color = TextPrimaryCyan, fontWeight = FontWeight.Bold)
                        Text(text = "Keeps JARVIS active in background", style = MaterialTheme.typography.labelSmall, color = TextMutedCyan)
                    }
                }
                Switch(
                    checked = foregroundActive,
                    onCheckedChange = {
                        foregroundActive = it
                        onToggleForegroundService(it)
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = ArcOrange, checkedTrackColor = ArcOrange.copy(alpha = 0.3f))
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Accessibility Screen Control Service Settings Button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurfaceCardBorder, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Accessibility, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.padding(start = 10.dp))
                    Column {
                        Text(text = "Accessibility Screen Control", style = MaterialTheme.typography.titleSmall, color = TextPrimaryCyan, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (JarvisAccessibilityService.isServiceEnabled()) "Status: ACTIVE" else "Status: INACTIVE (Enable in System Settings)",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (JarvisAccessibilityService.isServiceEnabled()) StatusGreen else ArcOrange
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceCardBorder),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Open Accessibility Settings", color = CyanPrimary, fontSize = 12.sp)
                }
            }
        }
    }
}

