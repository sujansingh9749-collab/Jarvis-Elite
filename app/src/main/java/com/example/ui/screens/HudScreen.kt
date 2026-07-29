package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.ArcReactorState
import com.example.ui.components.ArcReactorVisualizer
import com.example.ui.theme.ArcOrange
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.CyanSecondary
import com.example.ui.theme.DeepSpaceDark
import com.example.ui.theme.PlasmaPurple
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.TextMutedCyan
import com.example.ui.theme.TextPrimaryCyan
import com.example.ui.theme.TextSecondaryCyan

@Composable
fun HudScreen(
    currentLanguage: String, // "BN" or "EN"
    arcState: ArcReactorState,
    speechTranscript: String,
    jarvisResponse: String,
    isOnline: Boolean,
    volumeRms: Float,
    onLanguageToggle: () -> Unit,
    onMicClick: () -> Unit,
    onQuickAction: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpaceDark)
    ) {
        // Stark Blueprint HUD Image Background with subtle cyan atmosphere blend
        Image(
            painter = painterResource(id = R.drawable.img_stark_hud_bg),
            contentDescription = "Stark HUD Blueprint Background",
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.35f),
            contentScale = ContentScale.Crop
        )

        // Gradient overlay to keep text crisp and futuristic
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            DeepSpaceDark.copy(alpha = 0.85f),
                            Color(0xFF020714).copy(alpha = 0.60f),
                            DeepSpaceDark.copy(alpha = 0.95f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Stark Industries Header Branding
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STARK INDUSTRIES // MARK LXXXV",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanPrimary.copy(alpha = 0.8f),
                    letterSpacing = 2.sp
                )

                Text(
                    text = "SYS_ARMOR_100%",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = StatusGreen
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Immersive Telemetry Top Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, SurfaceCardBorder, RoundedCornerShape(16.dp)),
                color = SurfaceCard
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(if (isOnline) StatusGreen else ArcOrange)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "J.A.R.V.I.S. OS V4.5",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp
                                ),
                                color = CyanPrimary
                            )
                        }

                        // Language Switcher Pill
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { onLanguageToggle() }
                                .border(1.dp, CyanPrimary.copy(alpha = 0.6f), RoundedCornerShape(20.dp)),
                            color = DeepSpaceDark
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (currentLanguage == "BN") "BN (বাংলা)" else "EN (English)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyanPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Telemetry Data Chips Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TelemetryChip("CORE ENGINE", if (isOnline) "GEMINI 1.5" else "LOCAL LLM", if (isOnline) StatusGreen else ArcOrange)
                        TelemetryChip("MEMORY", "92% OK", CyanSecondary)
                        TelemetryChip("VOICE LOCK", "PROTECTED", PlasmaPurple)
                        TelemetryChip("STATE", if (arcState == ArcReactorState.IDLE) "STANDBY" else arcState.name, StatusGreen)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Central Animated Arc Reactor Visualizer
            ArcReactorVisualizer(
                state = arcState,
                volumeRms = volumeRms,
                onClick = onMicClick
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Audio Waveform Animating Bars
            if (arcState == ArcReactorState.LISTENING || arcState == ArcReactorState.SPEAKING) {
                AudioWaveformBars(state = arcState)
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Speech / Voice Status Indicator
            val statusText = when (arcState) {
                ArcReactorState.IDLE -> if (currentLanguage == "BN") "সিস্টেম প্রস্তুত - \"জার্ভিস\" বলুন বা কোরে চাপুন" else "SYSTEM STANDBY - Say \"Jarvis\" or Tap Core"
                ArcReactorState.LISTENING -> if (currentLanguage == "BN") "শুনছি... আপনার নির্দেশ বলুন" else "LISTENING... Speak your command"
                ArcReactorState.PROCESSING -> if (currentLanguage == "BN") "প্রসেসিং হচ্ছে..." else "ANALYZING INTENT..."
                ArcReactorState.SPEAKING -> if (currentLanguage == "BN") "জার্ভিস কথা বলছে..." else "JARVIS SPEAKING..."
            }

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, SurfaceCardBorder, RoundedCornerShape(20.dp)),
                color = SurfaceCard
            ) {
                Text(
                    text = statusText,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 9.dp),
                    color = when (arcState) {
                        ArcReactorState.IDLE -> TextSecondaryCyan
                        ArcReactorState.LISTENING -> StatusGreen
                        ArcReactorState.PROCESSING -> ArcOrange
                        ArcReactorState.SPEAKING -> PlasmaPurple
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Live User Speech Transcript Card
            if (speechTranscript.isNotBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(1.dp, SurfaceCardBorder, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (currentLanguage == "BN") "আপনি বলেছেন:" else "USER TRANSCRIPT:",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMutedCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = speechTranscript,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimaryCyan,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // JARVIS AI Response Card
            if (jarvisResponse.isNotBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(1.dp, CyanPrimary.copy(alpha = 0.7f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "J.A.R.V.I.S. RESPONSE:",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = { onQuickAction("replay_tts") },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Replay Speech",
                                    tint = CyanPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = jarvisResponse,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimaryCyan,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Multi-Lang Code-Mixing & Live Translator Feature Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(1.dp, CyanPrimary.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = "Translator",
                                tint = CyanPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "REAL-TIME CODE-MIX & TRANSLATOR",
                                style = MaterialTheme.typography.labelMedium,
                                color = CyanPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "LIVE ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = StatusGreen,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 9.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (currentLanguage == "BN")
                            "জার্ভিস বাংলা, ইংরেজি এবং কোড-মিক্সিং (যেমন: \"Jarvis, আমাকে weather টা বলো তো today-র\") সরাসরি বুঝতে পারে এবং সাথে সাথে লাইভ অনুবাদ করে।"
                        else
                            "JARVIS handles code-mixing (Banglish/English blend) and instant real-time live translation seamlessly.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryCyan,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onQuickAction("test_code_mix") }
                                .border(1.dp, SurfaceCardBorder, RoundedCornerShape(8.dp)),
                            color = DeepSpaceDark
                        ) {
                            Text(
                                text = "⚡ Test Banglish",
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanPrimary,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onQuickAction("test_translation") }
                                .border(1.dp, SurfaceCardBorder, RoundedCornerShape(8.dp)),
                            color = DeepSpaceDark
                        ) {
                            Text(
                                text = "🌐 Live Translate",
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = PlasmaPurple,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Cross-App Workflow Orchestrator Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(1.dp, ArcOrange.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = "Workflow Orchestrator",
                                tint = ArcOrange,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CROSS-APP WORKFLOW ENGINE",
                                style = MaterialTheme.typography.labelMedium,
                                color = ArcOrange,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "MULTI-APP READY",
                            style = MaterialTheme.typography.labelSmall,
                            color = StatusGreen,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 9.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (currentLanguage == "BN")
                            "মাল্টিপল অ্যাপ একসাথে সিকোয়েন্সিয়াল ও ক্রমান্বয়ে চালনা করুন (যেমন: \"Maps + YouTube + Volume boost\")."
                        else
                            "Chain multiple apps into unified automated workflows (e.g., Maps + Music + Audio Adjustments).",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryCyan,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onQuickAction("workflow_commute") }
                                .border(1.dp, SurfaceCardBorder, RoundedCornerShape(8.dp)),
                            color = DeepSpaceDark
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🚗 Commute Workflow",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyanPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Maps ➔ YouTube ➔ Vol Max",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMutedCyan,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onQuickAction("workflow_work") }
                                .border(1.dp, SurfaceCardBorder, RoundedCornerShape(8.dp)),
                            color = DeepSpaceDark
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "💼 Work & Study",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PlasmaPurple,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "WhatsApp ➔ Chrome ➔ Mute",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMutedCyan,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onQuickAction("workflow_social") }
                                .border(1.dp, SurfaceCardBorder, RoundedCornerShape(8.dp)),
                            color = DeepSpaceDark
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📸 Capture & Share",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StatusGreen,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Camera ➔ Gallery ➔ WhatsApp",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMutedCyan,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Quick System Action Buttons Grid
            Text(
                text = "SYSTEM CONTROLS & SHORTCUTS",
                style = MaterialTheme.typography.labelMedium,
                color = TextMutedCyan,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickControlButton("Flashlight", Icons.Default.FlashOn) { onQuickAction("flashlight") }
                QuickControlButton("Vol +", Icons.Default.VolumeUp) { onQuickAction("vol_up") }
                QuickControlButton("Vol -", Icons.Default.VolumeDown) { onQuickAction("vol_down") }
                QuickControlButton("WiFi", Icons.Default.Wifi) { onQuickAction("wifi") }
                QuickControlButton("Home", Icons.Default.Home) { onQuickAction("home") }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun TelemetryChip(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextMutedCyan, fontSize = 9.sp)
        Text(text = value, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold, fontSize = 11.sp)
    }
}

@Composable
fun AudioWaveformBars(state: ArcReactorState) {
    val infiniteTransition = rememberInfiniteTransition(label = "WaveformAnimation")

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(30.dp)
    ) {
        val barCount = 13
        for (i in 0 until barCount) {
            val animDuration = 280 + (i * 60)
            val barHeight by infiniteTransition.animateFloat(
                initialValue = 6f,
                targetValue = if (state == ArcReactorState.LISTENING || state == ArcReactorState.SPEAKING) (10 + (i % 6) * 4.5f) else 6f,
                animationSpec = infiniteRepeatable(
                    animation = tween(animDuration, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$i"
            )

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(barHeight.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (state == ArcReactorState.SPEAKING) PlasmaPurple else CyanPrimary)
            )
        }
    }
}

@Composable
fun QuickControlButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .border(1.dp, SurfaceCardBorder, RoundedCornerShape(14.dp)),
        color = SurfaceCard
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = CyanPrimary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondaryCyan,
                fontSize = 10.sp
            )
        }
    }
}

