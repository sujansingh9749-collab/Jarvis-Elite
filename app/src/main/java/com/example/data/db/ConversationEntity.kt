package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val prompt: String,
    val response: String,
    val timestamp: Long = System.currentTimeMillis(),
    val source: String = "LOCAL_AI",
    val language: String = "BN"
)
