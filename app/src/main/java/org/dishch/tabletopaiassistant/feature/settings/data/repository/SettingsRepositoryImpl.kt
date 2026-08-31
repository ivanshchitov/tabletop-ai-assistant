package org.dishch.tabletopaiassistant.feature.settings.data.repository

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import org.dishch.tabletopaiassistant.feature.settings.data.datasource.SettingsLocalDataSource
import org.dishch.tabletopaiassistant.feature.settings.domain.repository.SettingsRepository

class SettingsRepositoryImpl @Inject constructor(
    private val localDataSource: SettingsLocalDataSource,
) : SettingsRepository {

    override fun observeDialogCount(): Flow<Int> = localDataSource.observeDialogCount()

    override suspend fun clearHistory() {
        localDataSource.clearHistory()
    }
}
