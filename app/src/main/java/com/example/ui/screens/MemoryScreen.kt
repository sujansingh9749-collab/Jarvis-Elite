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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.MemoryFactEntity
import com.example.data.db.ReminderEntity
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
fun MemoryScreen(
    memoryFacts: List<MemoryFactEntity>,
    reminders: List<ReminderEntity>,
    onAddFact: (String, String) -> Unit,
    onDeleteFact: (Int) -> Unit,
    onAddReminder: (String, String) -> Unit,
    onToggleReminder: (Int, Boolean) -> Unit,
    onDeleteReminder: (Int) -> Unit
) {
    var showFactDialog by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpaceDark)
            .padding(16.dp)
    ) {
        Text(
            text = "JARVIS MEMORY & FACTS",
            style = MaterialTheme.typography.titleMedium,
            color = CyanPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Long-term persistent context and saved reminders",
            style = MaterialTheme.typography.labelSmall,
            color = TextMutedCyan
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Fact Dialog Triggers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { showFactDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp)
                    .border(1.dp, SurfaceCardBorder, RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.Memory, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.height(0.dp))
                Text(" Add Fact", color = TextPrimaryCyan, fontSize = 12.sp)
            }

            Button(
                onClick = { showReminderDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
                    .border(1.dp, SurfaceCardBorder, RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = ArcOrange, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.height(0.dp))
                Text(" Add Reminder", color = TextPrimaryCyan, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Facts Section
        Text(text = "LEARNED FACTS & PREFERENCES", style = MaterialTheme.typography.labelMedium, color = CyanPrimary, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))

        if (memoryFacts.isEmpty()) {
            Text(text = "No custom facts added yet.", style = MaterialTheme.typography.bodySmall, color = TextMutedCyan)
        } else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(memoryFacts) { fact ->
                    FactCard(fact = fact, onDelete = { onDeleteFact(fact.id) })
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Reminders Section
        Text(text = "ACTIVE REMINDERS & TIMERS", style = MaterialTheme.typography.labelMedium, color = ArcOrange, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))

        if (reminders.isEmpty()) {
            Text(text = "No reminders scheduled.", style = MaterialTheme.typography.bodySmall, color = TextMutedCyan)
        } else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(reminders) { reminder ->
                    ReminderCard(
                        reminder = reminder,
                        onToggle = { completed -> onToggleReminder(reminder.id, completed) },
                        onDelete = { onDeleteReminder(reminder.id) }
                    )
                }
            }
        }
    }

    // Add Fact Dialog
    if (showFactDialog) {
        var keyInput by remember { mutableStateOf("") }
        var valueInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showFactDialog = false },
            title = { Text("Add Memory Fact", color = CyanPrimary) },
            text = {
                Column {
                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it },
                        label = { Text("Fact Key (e.g., user_name)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanPrimary, unfocusedBorderColor = SurfaceCardBorder)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = valueInput,
                        onValueChange = { valueInput = it },
                        label = { Text("Fact Value (e.g., Tony Stark)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanPrimary, unfocusedBorderColor = SurfaceCardBorder)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (keyInput.isNotBlank() && valueInput.isNotBlank()) {
                            onAddFact(keyInput.trim(), valueInput.trim())
                            showFactDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) {
                    Text("Save", color = DeepSpaceDark)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFactDialog = false }) {
                    Text("Cancel", color = TextMutedCyan)
                }
            },
            containerColor = SurfaceCard
        )
    }

    // Add Reminder Dialog
    if (showReminderDialog) {
        var titleInput by remember { mutableStateOf("") }
        var timeInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showReminderDialog = false },
            title = { Text("Add Reminder", color = ArcOrange) },
            text = {
                Column {
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("Reminder Title") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ArcOrange, unfocusedBorderColor = SurfaceCardBorder)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = timeInput,
                        onValueChange = { timeInput = it },
                        label = { Text("Time (e.g., 08:00 AM)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ArcOrange, unfocusedBorderColor = SurfaceCardBorder)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (titleInput.isNotBlank()) {
                            onAddReminder(titleInput.trim(), if (timeInput.isBlank()) "Today" else timeInput.trim())
                            showReminderDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ArcOrange)
                ) {
                    Text("Save", color = DeepSpaceDark)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReminderDialog = false }) {
                    Text("Cancel", color = TextMutedCyan)
                }
            },
            containerColor = SurfaceCard
        )
    }
}

@Composable
fun FactCard(fact: MemoryFactEntity, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SurfaceCardBorder, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = fact.factKey, style = MaterialTheme.typography.labelMedium, color = CyanPrimary, fontWeight = FontWeight.Bold)
                Text(text = fact.factValue, style = MaterialTheme.typography.bodyMedium, color = TextPrimaryCyan)
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusRed, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun ReminderCard(reminder: ReminderEntity, onToggle: (Boolean) -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SurfaceCardBorder, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = reminder.isCompleted,
                    onCheckedChange = onToggle,
                    colors = CheckboxDefaults.colors(checkedColor = StatusGreen, uncheckedColor = TextMutedCyan)
                )
                Column {
                    Text(
                        text = reminder.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (reminder.isCompleted) TextMutedCyan else TextPrimaryCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = reminder.timeText, style = MaterialTheme.typography.labelSmall, color = ArcOrange)
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusRed, modifier = Modifier.size(16.dp))
            }
        }
    }
}
