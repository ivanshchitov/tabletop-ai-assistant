package org.dishch.tabletopaiassistant.feature.assistant.domain.repository

import kotlinx.coroutines.flow.Flow
import org.dishch.tabletopaiassistant.feature.assistant.domain.model.ChatMessage

interface AssistantRepository {

    fun observeHistory(): Flow<List<ChatMessage>>

    suspend fun saveMessage(message: ChatMessage)

    suspend fun askQuestion(question: String): String
}
