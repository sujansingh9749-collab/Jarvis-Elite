package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diagnostics")
data class DiagnosticEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val componentName: String,
    val status: String, // "HEALTHY", "WARNING", "ERROR"
    val message: String,
    val lastChecked: Long = System.currentTimeMillis()
)
