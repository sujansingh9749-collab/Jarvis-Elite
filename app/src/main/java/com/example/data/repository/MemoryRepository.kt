package com.example.data.repository

import com.example.data.db.ConversationEntity
import com.example.data.db.DiagnosticEntity
import com.example.data.db.JarvisDao
import com.example.data.db.MemoryFactEntity
import com.example.data.db.ReminderEntity
import kotlinx.coroutines.flow.Flow

class MemoryRepository(private val dao: JarvisDao) {
    val conversations: Flow<List<ConversationEntity>> = dao.getAllConversations()
    val memoryFacts: Flow<List<MemoryFactEntity>> = dao.getAllMemoryFacts()
    val reminders: Flow<List<ReminderEntity>> = dao.getAllReminders()
    val diagnostics: Flow<List<DiagnosticEntity>> = dao.getAllDiagnostics()

    suspend fun saveConversation(prompt: String, response: String, source: String, language: String) {
        dao.insertConversation(
            ConversationEntity(
                prompt = prompt,
                response = response,
                source = source,
                language = language
            )
        )
    }

    suspend fun clearHistory() = dao.clearConversations()
    suspend fun deleteConversation(id: Int) = dao.deleteConversation(id)

    suspend fun saveFact(key: String, value: String, category: String = "LEARNED") {
        dao.insertMemoryFact(
            MemoryFactEntity(
                factKey = key,
                factValue = value,
                category = category
            )
        )
    }

    suspend fun getFact(key: String): String? {
        return dao.getMemoryFactByKey(key)?.factValue
    }

    suspend fun deleteFact(id: Int) = dao.deleteMemoryFact(id)

    suspend fun addReminder(title: String, timeText: String) {
        dao.insertReminder(ReminderEntity(title = title, timeText = timeText))
    }

    suspend fun toggleReminder(id: Int, completed: Boolean) {
        dao.setReminderCompleted(id, completed)
    }

    suspend fun deleteReminder(id: Int) = dao.deleteReminder(id)

    suspend fun logDiagnostic(component: String, status: String, message: String) {
        dao.insertDiagnostic(
            DiagnosticEntity(
                componentName = component,
                status = status,
                message = message
            )
        )
    }

    suspend fun clearDiagnostics() = dao.clearDiagnostics()
}
