package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface JarvisDao {
    // Conversations
    @Query("SELECT * FROM conversations ORDER BY timestamp DESC")
    fun getAllConversations(): Flow<List<ConversationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)

    @Query("DELETE FROM conversations")
    suspend fun clearConversations()

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteConversation(id: Int)

    // Memory Facts
    @Query("SELECT * FROM memory_facts ORDER BY updatedAt DESC")
    fun getAllMemoryFacts(): Flow<List<MemoryFactEntity>>

    @Query("SELECT * FROM memory_facts WHERE factKey = :key LIMIT 1")
    suspend fun getMemoryFactByKey(key: String): MemoryFactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemoryFact(fact: MemoryFactEntity)

    @Query("DELETE FROM memory_facts WHERE id = :id")
    suspend fun deleteMemoryFact(id: Int)

    // Reminders
    @Query("SELECT * FROM reminders ORDER BY isCompleted ASC, createdAt DESC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity)

    @Query("UPDATE reminders SET isCompleted = :completed WHERE id = :id")
    suspend fun setReminderCompleted(id: Int, completed: Boolean)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminder(id: Int)

    // Diagnostics
    @Query("SELECT * FROM diagnostics ORDER BY lastChecked DESC")
    fun getAllDiagnostics(): Flow<List<DiagnosticEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiagnostic(diagnostic: DiagnosticEntity)

    @Query("DELETE FROM diagnostics")
    suspend fun clearDiagnostics()
}
