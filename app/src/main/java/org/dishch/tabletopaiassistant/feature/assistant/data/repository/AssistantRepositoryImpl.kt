package org.dishch.tabletopaiassistant.feature.assistant.data.repository

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.dishch.tabletopaiassistant.feature.assistant.data.datasource.AssistantLocalDataSource
import org.dishch.tabletopaiassistant.feature.assistant.data.datasource.AssistantRemoteDataSource
import org.dishch.tabletopaiassistant.feature.assistant.data.mapper.toAnswerText
import org.dishch.tabletopaiassistant.feature.assistant.data.mapper.toDomain
import org.dishch.tabletopaiassistant.feature.assistant.data.mapper.toEntity
import org.dishch.tabletopaiassistant.feature.assistant.domain.model.ChatMessage
import org.dishch.tabletopaiassistant.feature.assistant.domain.repository.AssistantRepository

class AssistantRepositoryImpl @Inject constructor(
    private val remoteDataSource: AssistantRemoteDataSource,
    private val localDataSource: AssistantLocalDataSource,
) : AssistantRepository {

    override fun observeHistory(): Flow<List<ChatMessage>> =
        localDataSource.observeLastMessages().map { entities -> entities.map { it.toDomain() } }

    override suspend fun saveMessage(message: ChatMessage) {
        localDataSource.insertMessage(message.toEntity())
    }

    override suspend fun askQuestion(question: String): String =
        remoteDataSource.askQuestion(question).toAnswerText()
}
