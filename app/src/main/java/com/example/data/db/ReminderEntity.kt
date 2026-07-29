package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val timeText: String,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
