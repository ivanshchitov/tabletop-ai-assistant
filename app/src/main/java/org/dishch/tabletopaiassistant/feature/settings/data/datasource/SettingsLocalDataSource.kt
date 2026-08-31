package org.dishch.tabletopaiassistant.feature.settings.data.datasource

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import org.dishch.tabletopaiassistant.core.database.dao.ChatMessageDao

class SettingsLocalDataSource @Inject constructor(
    private val chatMessageDao: ChatMessageDao,
) {

    fun observeDialogCount(): Flow<Int> = chatMessageDao.observeDialogCount()

    suspend fun clearHistory() = chatMessageDao.deleteAllMessages()
}
