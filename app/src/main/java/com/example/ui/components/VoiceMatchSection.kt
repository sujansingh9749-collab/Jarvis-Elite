package com.example.ui.components

import android.widget.Toast
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextAlign
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
import com.example.voice.SpeechManager
import com.example.voice.VoiceMatchManager
import com.example.voice.VoicePrintProfile
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VoiceMatchSection(
    voiceMatchManager: VoiceMatchManager,
    speechManager: SpeechManager?,
    currentLanguage: String,
    onLockToggled: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var isLockEnabled by remember { mutableStateOf(voiceMatchManager.isLockEnabled) }
    var currentThreshold by remember { mutableIntStateOf(voiceMatchManager.matchThresholdPercent) }
    var profileState by remember { mutableStateOf(voiceMatchManager.getVoiceProfile()) }

    // Enrollment Wizard States
    var showEnrollmentWizard by remember { mutableStateOf(false) }
    var enrollmentStep by remember { mutableIntStateOf(1) } // 1, 2, 3
    var isEnrollRecording by remember { mutableStateOf(false) }
    val capturedStepRmsList = remember { mutableListOf<Float>() }

    // Verification Test States
    var isTestRecording by remember { mutableStateOf(false) }
    var testResultText by remember { mutableStateOf<String?>(null) }
    var testResultConfidence by remember { mutableIntStateOf(-1) }

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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SurfaceCardBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            // Header Row: Voice Match Security Lock Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Voice Match Biometrics",
                        tint = CyanPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (currentLanguage == "BN") "ভয়েস ম্যাচ সিকিউরিটি লক" else "Voice Match Owner Lock",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimaryCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (currentLanguage == "BN") "শুধুমাত্র অনুমোদিত মালিকের ভয়েস প্রিন্ট ম্যাচ করলে কাজ করবে" else "Restrict access strictly to the owner's voice print",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMutedCyan,
                            fontSize = 10.sp
                        )
                    }
                }

                Switch(
                    checked = isLockEnabled,
                    onCheckedChange = { checked ->
                        if (checked && profileState == null) {
                            Toast.makeText(context, "Please enroll your voice print profile first!", Toast.LENGTH_LONG).show()
                            showEnrollmentWizard = true
                        } else {
                            isLockEnabled = checked
                            voiceMatchManager.isLockEnabled = checked
                            onLockToggled(checked)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CyanPrimary,
                        checkedTrackColor = CyanPrimary.copy(alpha = 0.3f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Voice Print Enrollment Status Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(
                        1.dp,
                        if (profileState != null) StatusGreen.copy(alpha = 0.5f) else ArcOrange.copy(alpha = 0.5f),
                        RoundedCornerShape(10.dp)
                    ),
                color = DeepSpaceDark
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (profileState != null) StatusGreen else ArcOrange)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (profileState != null) "VOICE PRINT ENROLLED" else "NO VOICE PRINT REGISTERED",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (profileState != null) StatusGreen else ArcOrange,
                                fontWeight = FontWeight.Bold
                            )
                            if (profileState != null) {
                                Text(
                                    text = "Owner: ${profileState?.ownerName} • Enrolled Profile Active",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMutedCyan,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    Row {
                        if (profileState != null) {
                            IconButton(
                                onClick = {
                                    voiceMatchManager.clearVoiceProfile()
                                    profileState = null
                                    isLockEnabled = false
                                    Toast.makeText(context, "Voice print profile cleared", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = StatusRed, modifier = Modifier.size(18.dp))
                            }
                        }

                        Button(
                            onClick = {
                                showEnrollmentWizard = true
                                enrollmentStep = 1
                                isEnrollRecording = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = if (profileState != null) "Re-Enroll" else "Enroll Now",
                                color = DeepSpaceDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sensitivity / Threshold Selector Row
            Text(
                text = "Security Match Sensitivity Level:",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondaryCyan,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val thresholds = listOf(
                    65 to "Standard (65%)",
                    75 to "High Security (75%)",
                    85 to "Maximum Security (85%)"
                )

                thresholds.forEach { (thresh, label) ->
                    val isSelected = currentThreshold == thresh
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, if (isSelected) CyanPrimary else SurfaceCardBorder, RoundedCornerShape(8.dp))
                            .clickable {
                                currentThreshold = thresh
                                voiceMatchManager.matchThresholdPercent = thresh
                            },
                        color = if (isSelected) CyanPrimary.copy(alpha = 0.2f) else DeepSpaceDark
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) CyanPrimary else TextMutedCyan,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // --- 1. ENROLLMENT WIZARD MODAL / EXPANDABLE CARD ---
            AnimatedVisibility(visible = showEnrollmentWizard) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(DeepSpaceDark)
                        .border(1.dp, CyanPrimary.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "VOICE PRINT ENROLLMENT WIZARD",
                            style = MaterialTheme.typography.titleSmall,
                            color = CyanPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Step $enrollmentStep / 3",
                            style = MaterialTheme.typography.labelSmall,
                            color = StatusGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val samplePhrase = when (enrollmentStep) {
                        1 -> if (currentLanguage == "BN") "\"জার্ভিস ভয়েস প্রিন্ট এনরোল করো\"" else "\"Jarvis initialize system voice print\""
                        2 -> if (currentLanguage == "BN") "\"অ্যাক্সেস কোড আলফা কনফার্ম করো\"" else "\"Voice authorization security protocol\""
                        else -> if (currentLanguage == "BN") "\"জার্ভিস সিস্টেম রিএক্টিভেট করো\"" else "\"Jarvis standing by for owner command\""
                    }

                    Text(
                        text = "Press record and speak the phrase aloud clearly:",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMutedCyan
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = samplePhrase,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimaryCyan,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { showEnrollmentWizard = false },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Cancel", fontSize = 11.sp, color = TextMutedCyan)
                        }

                        if (!isEnrollRecording) {
                            Button(
                                onClick = {
                                    isEnrollRecording = true
                                    speechManager?.startListening(currentLanguage)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = "Record", tint = DeepSpaceDark, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Speak Phrase $enrollmentStep", color = DeepSpaceDark, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        } else {
                            Button(
                                onClick = {
                                    speechManager?.stopListening()
                                    val capturedRms = speechManager?.getCapturedRmsList() ?: emptyList()
                                    capturedStepRmsList.addAll(capturedRms)
                                    isEnrollRecording = false

                                    if (enrollmentStep < 3) {
                                        enrollmentStep++
                                        Toast.makeText(context, "Step completed! Speak next phrase.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        // Complete Enrollment
                                        val generatedProfile = voiceMatchManager.extractMetricsFromRmsList(capturedStepRmsList)
                                        voiceMatchManager.saveVoiceProfile(generatedProfile)
                                        profileState = generatedProfile
                                        isLockEnabled = true
                                        voiceMatchManager.isLockEnabled = true
                                        showEnrollmentWizard = false
                                        Toast.makeText(context, "Voice print enrollment complete! Lock activated.", Toast.LENGTH_LONG).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.scale(pulseScale)
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = "Done", tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Stop & Save Step", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- 2. LIVE TEST VOICE MATCH VERIFICATION ---
            OutlinedButton(
                onClick = {
                    isTestRecording = true
                    testResultText = null
                    testResultConfidence = -1
                    speechManager?.startListening(currentLanguage)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanPrimary)
            ) {
                Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isTestRecording) "Listening... Speak to verify voice" else "Test Live Voice Print Match",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isTestRecording) {
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = {
                        speechManager?.stopListening()
                        isTestRecording = false
                        val capturedRms = speechManager?.getCapturedRmsList() ?: emptyList()
                        val result = voiceMatchManager.verifySpeaker(capturedRms, "test phrase")
                        testResultConfidence = result.confidencePercent
                        testResultText = if (result.isVerified) {
                            "VERIFIED OWNER (${result.confidencePercent}% Match)"
                        } else {
                            "ACCESS DENIED (${result.confidencePercent}% Match < ${currentThreshold}%)"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ArcOrange),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Evaluate Voice Match", color = DeepSpaceDark, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }

            if (testResultText != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)),
                    color = if (testResultConfidence >= currentThreshold) StatusGreen.copy(alpha = 0.2f) else StatusRed.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = testResultText ?: "",
                        modifier = Modifier.padding(10.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (testResultConfidence >= currentThreshold) StatusGreen else StatusRed,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
