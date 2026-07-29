package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DeepSpaceDark
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.TextMutedCyan
import com.example.ui.theme.TextPrimaryCyan

sealed class JarvisTab(val route: String, val title: String, val icon: ImageVector) {
    object HUD : JarvisTab("hud", "HUD", Icons.Default.Mic)
    object History : JarvisTab("history", "History", Icons.Default.History)
    object Memory : JarvisTab("memory", "Memory", Icons.Default.Memory)
    object Diagnostics : JarvisTab("diagnostics", "Health", Icons.Default.Build)
    object Settings : JarvisTab("settings", "Settings", Icons.Default.Settings)
}

@Composable
fun JarvisBottomNav(
    currentRoute: String,
    onTabSelected: (JarvisTab) -> Unit
) {
    val tabs = listOf(
        JarvisTab.HUD,
        JarvisTab.History,
        JarvisTab.Memory,
        JarvisTab.Diagnostics,
        JarvisTab.Settings
    )

    NavigationBar(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, SurfaceCardBorder, RoundedCornerShape(24.dp)),
        containerColor = SurfaceCard,
        contentColor = TextPrimaryCyan,
        tonalElevation = 8.dp
    ) {
        tabs.forEach { tab ->
            val isSelected = currentRoute == tab.route
            val iconTint by animateColorAsState(
                targetValue = if (isSelected) CyanPrimary else TextMutedCyan,
                animationSpec = tween(250),
                label = "iconTint"
            )

            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        tint = iconTint
                    )
                },
                label = {
                    Text(
                        text = tab.title,
                        color = iconTint,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = CyanPrimary.copy(alpha = 0.22f),
                    selectedIconColor = CyanPrimary,
                    selectedTextColor = CyanPrimary,
                    unselectedIconColor = TextMutedCyan,
                    unselectedTextColor = TextMutedCyan
                )
            )
        }
    }
}
