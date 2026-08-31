package org.dishch.tabletopaiassistant.feature.settings.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    fun observeDialogCount(): Flow<Int>

    suspend fun clearHistory()
}
