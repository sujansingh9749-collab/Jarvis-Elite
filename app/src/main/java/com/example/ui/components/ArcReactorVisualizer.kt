package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ArcGold
import com.example.ui.theme.ArcOrange
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.CyanSecondary
import com.example.ui.theme.DeepSpaceDark
import com.example.ui.theme.PlasmaPurple
import com.example.ui.theme.StatusGreen
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class ArcReactorState {
    IDLE, LISTENING, PROCESSING, SPEAKING
}

@Composable
fun ArcReactorVisualizer(
    state: ArcReactorState,
    volumeRms: Float = 0f,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ArcReactorRotation")

    // Primary rotation angle
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Secondary rapid counter-rotation
    val counterRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "counterRotation"
    )

    // Pulsing scale bound to state and audio volume RMS
    val basePulseTarget = when (state) {
        ArcReactorState.IDLE -> 1.02f
        ArcReactorState.LISTENING -> 1.12f + (volumeRms.coerceIn(0f, 20f) / 100f)
        ArcReactorState.PROCESSING -> 1.18f
        ArcReactorState.SPEAKING -> 1.14f
    }

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = basePulseTarget,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (state == ArcReactorState.LISTENING) 400 else 1400,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val activeColor = when (state) {
        ArcReactorState.IDLE -> CyanPrimary
        ArcReactorState.LISTENING -> Color(0xFF00FFCC)
        ArcReactorState.PROCESSING -> ArcOrange
        ArcReactorState.SPEAKING -> PlasmaPurple
    }

    val glowColor = when (state) {
        ArcReactorState.IDLE -> CyanSecondary
        ArcReactorState.LISTENING -> StatusGreen
        ArcReactorState.PROCESSING -> ArcGold
        ArcReactorState.SPEAKING -> CyanPrimary
    }

    Box(
        modifier = Modifier
            .size(250.dp)
            .scale(pulseScale)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        activeColor.copy(alpha = 0.35f),
                        glowColor.copy(alpha = 0.15f),
                        DeepSpaceDark.copy(alpha = 0.95f)
                    )
                )
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val baseRadius = size.width / 2.4f

            // 1. Ambient Outer Halo Ring
            drawCircle(
                color = activeColor.copy(alpha = 0.2f),
                radius = baseRadius * 1.18f,
                center = center,
                style = Stroke(width = 8f)
            )

            // 2. Outer Rotating Tech Nodes Ring
            rotate(rotationAngle, pivot = center) {
                drawCircle(
                    color = activeColor,
                    radius = baseRadius,
                    center = center,
                    style = Stroke(
                        width = 4f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(24f, 16f), 0f)
                    )
                )

                // Orbiting Node Dots
                val nodeCount = 8
                for (i in 0 until nodeCount) {
                    val angle = (i * (360f / nodeCount)) * (PI / 180f)
                    val nx = center.x + baseRadius * cos(angle).toFloat()
                    val ny = center.y + baseRadius * sin(angle).toFloat()
                    drawCircle(
                        color = glowColor,
                        radius = 4f,
                        center = Offset(nx, ny)
                    )
                }
            }

            // 3. Counter-Rotating Inner Precision Arc Segments
            rotate(counterRotation, pivot = center) {
                drawCircle(
                    color = glowColor.copy(alpha = 0.85f),
                    radius = baseRadius * 0.82f,
                    center = center,
                    style = Stroke(
                        width = 6f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(50f, 30f), 0f)
                    )
                )
            }

            // 4. Radial Power Conduits / Rays
            val rayCount = 12
            for (i in 0 until rayCount) {
                val angle = (i * (360f / rayCount) + rotationAngle * 0.4f) * (PI / 180f)
                val startX = center.x + (baseRadius * 0.48f) * cos(angle).toFloat()
                val startY = center.y + (baseRadius * 0.48f) * sin(angle).toFloat()
                val endX = center.x + (baseRadius * 0.78f) * cos(angle).toFloat()
                val endY = center.y + (baseRadius * 0.78f) * sin(angle).toFloat()

                drawLine(
                    color = activeColor.copy(alpha = 0.65f),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 3.5f
                )
            }

            // 5. Central Reactor Core Glowing Ring
            drawCircle(
                color = activeColor,
                radius = baseRadius * 0.45f,
                center = center,
                style = Stroke(width = 4f)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        activeColor.copy(alpha = 0.6f),
                        activeColor.copy(alpha = 0.1f)
                    )
                ),
                radius = baseRadius * 0.42f,
                center = center
            )
        }

        // Center Vibrating Holographic Icon
        Icon(
            imageVector = if (state == ArcReactorState.SPEAKING) Icons.Default.GraphicEq else Icons.Default.Mic,
            contentDescription = "JARVIS Voice Trigger",
            tint = Color.White,
            modifier = Modifier.size(50.dp)
        )
    }
}
