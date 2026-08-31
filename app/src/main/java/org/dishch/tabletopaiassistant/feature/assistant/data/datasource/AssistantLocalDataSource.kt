package org.dishch.tabletopaiassistant.feature.assistant.data.datasource

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import org.dishch.tabletopaiassistant.core.database.dao.ChatMessageDao
import org.dishch.tabletopaiassistant.core.database.entity.ChatMessageEntity

class AssistantLocalDataSource @Inject constructor(
    private val chatMessageDao: ChatMessageDao,
) {

    fun observeLastMessages(): Flow<List<ChatMessageEntity>> = chatMessageDao.observeLastMessages()

    suspend fun insertMessage(entity: ChatMessageEntity) = chatMessageDao.insertMessage(entity)
}
