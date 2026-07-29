package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memory_facts")
data class MemoryFactEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val factKey: String,
    val factValue: String,
    val category: String = "PREFERENCE",
    val updatedAt: Long = System.currentTimeMillis()
)
