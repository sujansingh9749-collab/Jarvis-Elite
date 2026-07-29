package com.example.ui.components

import android.net.Uri
import android.speech.tts.Voice
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ArcOrange
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.CyanSecondary
import com.example.ui.theme.DeepSpaceDark
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.TextMutedCyan
import com.example.ui.theme.TextPrimaryCyan
import com.example.ui.theme.TextSecondaryCyan
import com.example.voice.CustomVoiceManager
import com.example.voice.ResponseKeyInfo
import com.example.voice.VoiceConstants
import com.example.voice.VoicePreset
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VoiceCustomizationSection(
    currentPresetId: String,
    currentRate: Float,
    currentPitch: Float,
    enableCustomClips: Boolean,
    availableSystemVoices: List<Voice>,
    selectedSystemVoiceName: String?,
    customVoiceManager: CustomVoiceManager,
    onSelectPreset: (VoicePreset) -> Unit,
    onUpdateVoiceParams: (Float, Float) -> Unit,
    onSelectSystemVoice: (String?) -> Unit,
    onToggleCustomClips: (Boolean) -> Unit,
    onPreviewPreset: (VoicePreset) -> Unit,
    onPreviewClip: (String) -> Unit
) {
    val context = LocalContext.current
    var rateValue by remember { mutableFloatStateOf(currentRate) }
    var pitchValue by remember { mutableFloatStateOf(currentPitch) }

    var recordingKey by remember { mutableStateOf<String?>(null) }
    var recordingTimerSeconds by remember { mutableIntStateOf(0) }
    var playingKey by remember { mutableStateOf<String?>(null) }

    // State map to trigger recompositions when custom clips change
    var clipUpdateTrigger by remember { mutableIntStateOf(0) }

    // Live Recording Timer Coroutine
    LaunchedEffect(recordingKey) {
        if (recordingKey != null) {
            recordingTimerSeconds = 0
            while (recordingKey != null) {
                delay(1000)
                recordingTimerSeconds++
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {

        // --- 1. PRE-INSTALLED VOICE PRESETS CARD ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurfaceCardBorder, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "J.A.R.V.I.S. Voice Engine Profiles", style = MaterialTheme.typography.titleSmall, color = TextPrimaryCyan, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Select a pre-configured AI voice signature or fine-tune sliders.",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMutedCyan
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Presets Grid / FlowRow
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    VoiceConstants.ALL_PRESETS.forEach { preset ->
                        val isSelected = preset.id == currentPresetId
                        val borderColor by animateColorAsState(if (isSelected) CyanPrimary else SurfaceCardBorder, label = "borderColor")
                        val containerBg by animateColorAsState(if (isSelected) CyanPrimary.copy(alpha = 0.15f) else DeepSpaceDark, label = "bgColor")

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                                .clickable {
                                    onSelectPreset(preset)
                                    rateValue = preset.speechRate
                                    pitchValue = preset.pitch
                                },
                            color = containerBg
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isSelected) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Column {
                                    Text(
                                        text = preset.name,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (isSelected) CyanPrimary else TextPrimaryCyan,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                    Text(
                                        text = preset.description,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMutedCyan,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Preview Preset Button
                Button(
                    onClick = {
                        val activePreset = VoiceConstants.ALL_PRESETS.find { it.id == currentPresetId } ?: VoiceConstants.PRESET_JARVIS_CLASSIC
                        onPreviewPreset(activePreset)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = DeepSpaceDark, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Test Selected Voice", color = DeepSpaceDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 2. FINE-TUNE PITCH & SPEED SLIDERS ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurfaceCardBorder, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(text = "Voice Frequency & Speed Tuning", style = MaterialTheme.typography.titleSmall, color = CyanPrimary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Speech Pace: ${String.format("%.2f", rateValue)}x", style = MaterialTheme.typography.labelMedium, color = TextSecondaryCyan)
                Slider(
                    value = rateValue,
                    onValueChange = {
                        rateValue = it
                        onUpdateVoiceParams(rateValue, pitchValue)
                    },
                    valueRange = 0.5f..2.0f,
                    colors = SliderDefaults.colors(thumbColor = CyanPrimary, activeTrackColor = CyanPrimary)
                )

                Text(text = "Pitch Tone: ${String.format("%.2f", pitchValue)}x", style = MaterialTheme.typography.labelMedium, color = TextSecondaryCyan)
                Slider(
                    value = pitchValue,
                    onValueChange = {
                        pitchValue = it
                        onUpdateVoiceParams(rateValue, pitchValue)
                    },
                    valueRange = 0.5f..2.0f,
                    colors = SliderDefaults.colors(thumbColor = ArcOrange, activeTrackColor = ArcOrange)
                )
            }
        }

        // --- 3. SYSTEM TTS VOICES (IF AVAILABLE) ---
        if (availableSystemVoices.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SurfaceCardBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "Installed System Audio Engines", style = MaterialTheme.typography.titleSmall, color = TextPrimaryCyan, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Select native TTS voices provided by your device OS.", style = MaterialTheme.typography.labelSmall, color = TextMutedCyan)

                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Option for Auto/Default
                        val isDefaultSelected = selectedSystemVoiceName == null
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, if (isDefaultSelected) CyanPrimary else SurfaceCardBorder, RoundedCornerShape(8.dp))
                                .clickable { onSelectSystemVoice(null) },
                            color = if (isDefaultSelected) CyanPrimary.copy(alpha = 0.2f) else DeepSpaceDark
                        ) {
                            Text(
                                text = "Auto Engine",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isDefaultSelected) CyanPrimary else TextSecondaryCyan,
                                fontWeight = if (isDefaultSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }

                        availableSystemVoices.take(8).forEach { voice ->
                            val isVoiceSelected = selectedSystemVoiceName == voice.name
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, if (isVoiceSelected) CyanPrimary else SurfaceCardBorder, RoundedCornerShape(8.dp))
                                    .clickable { onSelectSystemVoice(voice.name) },
                                color = if (isVoiceSelected) CyanPrimary.copy(alpha = 0.2f) else DeepSpaceDark
                            ) {
                                Text(
                                    text = "${voice.locale.language.uppercase()}-${voice.name.takeLast(6)}",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isVoiceSelected) CyanPrimary else TextSecondaryCyan,
                                    fontWeight = if (isVoiceSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 4. CUSTOM VOICE RESPONSE CLIPS (RECORD & UPLOAD) ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurfaceCardBorder, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AudioFile, contentDescription = null, tint = ArcOrange, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = "Custom Response Audio Clips", style = MaterialTheme.typography.titleSmall, color = TextPrimaryCyan, fontWeight = FontWeight.Bold)
                            Text(text = "Record or upload custom voice files for common responses", style = MaterialTheme.typography.labelSmall, color = TextMutedCyan)
                        }
                    }

                    Switch(
                        checked = enableCustomClips,
                        onCheckedChange = { onToggleCustomClips(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = ArcOrange, checkedTrackColor = ArcOrange.copy(alpha = 0.3f))
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // List of response keys
                VoiceConstants.RESPONSE_KEYS.forEach { responseInfo ->
                    // Force state read
                    val keyTrigger = clipUpdateTrigger
                    val hasCustomClip = customVoiceManager.hasCustomClip(responseInfo.key)
                    val isRecordingThis = recordingKey == responseInfo.key

                    ResponseClipItemRow(
                        responseInfo = responseInfo,
                        hasCustomClip = hasCustomClip,
                        isRecording = isRecordingThis,
                        recordingTimerSeconds = if (isRecordingThis) recordingTimerSeconds else 0,
                        isPlaying = playingKey == responseInfo.key,
                        onStartRecord = {
                            val success = customVoiceManager.startRecording(responseInfo.key)
                            if (success) {
                                recordingKey = responseInfo.key
                            } else {
                                Toast.makeText(context, "Microphone recording failed", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onStopRecord = {
                            val file = customVoiceManager.stopRecording()
                            recordingKey = null
                            clipUpdateTrigger++
                            if (file != null) {
                                Toast.makeText(context, "Custom voice clip saved!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onSaveUploadedUri = { uri ->
                            val success = customVoiceManager.saveUploadedClip(responseInfo.key, uri)
                            clipUpdateTrigger++
                            if (success) {
                                Toast.makeText(context, "Custom audio file imported successfully!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Failed to import audio file", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onPlayClip = {
                            playingKey = responseInfo.key
                            onPreviewClip(responseInfo.key)
                        },
                        onDeleteClip = {
                            customVoiceManager.deleteClip(responseInfo.key)
                            clipUpdateTrigger++
                            Toast.makeText(context, "Clip reset to default TTS", Toast.LENGTH_SHORT).show()
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun ResponseClipItemRow(
    responseInfo: ResponseKeyInfo,
    hasCustomClip: Boolean,
    isRecording: Boolean,
    recordingTimerSeconds: Int,
    isPlaying: Boolean,
    onStartRecord: () -> Unit,
    onStopRecord: () -> Unit,
    onSaveUploadedUri: (Uri) -> Unit,
    onPlayClip: () -> Unit,
    onDeleteClip: () -> Unit
) {
    // ActivityResultLauncher for audio file picker
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onSaveUploadedUri(uri)
        }
    }

    // Recording Pulse Animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = 1.dp,
                color = if (isRecording) StatusRed else if (hasCustomClip) StatusGreen.copy(alpha = 0.6f) else SurfaceCardBorder,
                shape = RoundedCornerShape(10.dp)
            ),
        color = DeepSpaceDark
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = responseInfo.title,
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimaryCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = responseInfo.description,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMutedCyan,
                        fontSize = 10.sp
                    )
                }

                // Status Badge
                Surface(
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                    color = if (isRecording) StatusRed.copy(alpha = 0.2f) else if (hasCustomClip) StatusGreen.copy(alpha = 0.2f) else SurfaceCardBorder.copy(alpha = 0.3f)
                ) {
                    Text(
                        text = if (isRecording) "REC 00:0${recordingTimerSeconds}" else if (hasCustomClip) "CUSTOM CLIP" else "TTS SYNTHETIC",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isRecording) StatusRed else if (hasCustomClip) StatusGreen else TextMutedCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Interactive Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Record / Stop Record Button
                if (isRecording) {
                    Button(
                        onClick = onStopRecord,
                        colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.scale(pulseScale)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = "Stop", tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Stop (${recordingTimerSeconds}s)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    OutlinedButton(
                        onClick = onStartRecord,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanPrimary)
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = "Record", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Record Voice", fontSize = 11.sp)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Upload Custom Audio File Button
                    IconButton(
                        onClick = { filePickerLauncher.launch("audio/*") },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = "Upload Audio", tint = ArcOrange, modifier = Modifier.size(20.dp))
                    }

                    // Play Preview Button (If Custom Clip Exists)
                    if (hasCustomClip) {
                        IconButton(
                            onClick = onPlayClip,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.GraphicEq else Icons.Default.PlayArrow,
                                contentDescription = "Play Clip",
                                tint = StatusGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Delete Custom Clip Button
                        IconButton(
                            onClick = onDeleteClip,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Clip", tint = StatusRed, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}
