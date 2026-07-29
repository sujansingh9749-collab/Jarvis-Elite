package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val JarvisColorScheme = darkColorScheme(
    primary = CyanPrimary,
    onPrimary = DeepSpaceDark,
    primaryContainer = SurfaceCard,
    onPrimaryContainer = TextPrimaryCyan,
    secondary = CyanSecondary,
    onSecondary = DeepSpaceDark,
    secondaryContainer = SurfaceCardBorder,
    onSecondaryContainer = TextPrimaryCyan,
    tertiary = ArcOrange,
    onTertiary = DeepSpaceDark,
    background = DeepSpaceDark,
    onBackground = TextPrimaryCyan,
    surface = SurfaceDark,
    onSurface = TextPrimaryCyan,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondaryCyan,
    outline = SurfaceCardBorder,
    error = StatusRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Always enforce JARVIS HUD theme
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = JarvisColorScheme,
        typography = Typography,
        content = content
    )
}
