package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.system.DiagnosticResult
import com.example.ui.theme.ArcOrange
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DeepSpaceDark
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.TextMutedCyan
import com.example.ui.theme.TextPrimaryCyan
import com.example.ui.theme.TextSecondaryCyan

@Composable
fun DiagnosticsScreen(
    diagnosticResults: List<DiagnosticResult>,
    isRunning: Boolean,
    onRunDiagnostics: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpaceDark)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AUTO-REPAIR DIAGNOSTICS",
                    style = MaterialTheme.typography.titleMedium,
                    color = CyanPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "System integrity and sub-system health check",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMutedCyan
                )
            }

            Button(
                onClick = onRunDiagnostics,
                enabled = !isRunning,
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Run", tint = DeepSpaceDark, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.height(0.dp))
                Text(if (isRunning) "Scanning..." else "Run Diagnostics", color = DeepSpaceDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (diagnosticResults.isEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
                    .border(1.dp, SurfaceCardBorder, RoundedCornerShape(12.dp)),
                color = SurfaceCard
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Build, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("NO DIAGNOSTIC LOGS YET", style = MaterialTheme.typography.titleMedium, color = TextPrimaryCyan, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Tap 'Run Diagnostics' to perform a complete system health scan.", style = MaterialTheme.typography.bodySmall, color = TextSecondaryCyan)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(diagnosticResults) { result ->
                    DiagnosticCard(result = result)
                }
            }
        }
    }
}

@Composable
fun DiagnosticCard(result: DiagnosticResult) {
    val statusColor = when (result.status) {
        "HEALTHY" -> StatusGreen
        "WARNING" -> ArcOrange
        else -> StatusRed
    }

    val statusIcon = when (result.status) {
        "HEALTHY" -> Icons.Default.CheckCircle
        "WARNING" -> Icons.Default.Warning
        else -> Icons.Default.Error
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = statusIcon, contentDescription = result.status, tint = statusColor, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.padding(start = 12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = result.component, style = MaterialTheme.typography.titleSmall, color = TextPrimaryCyan, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = result.detail, style = MaterialTheme.typography.bodySmall, color = TextSecondaryCyan)
                if (!result.repairAction.isNull_or_empty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Action: ${result.repairAction}", style = MaterialTheme.typography.labelSmall, color = statusColor, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun String?.isNull_or_empty(): Boolean = this == null || this.trim().isEmpty()
