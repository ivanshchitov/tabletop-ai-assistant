package org.dishch.tabletopaiassistant.feature.settings.domain.usecase

import javax.inject.Inject
import org.dishch.tabletopaiassistant.feature.settings.domain.error.SettingsError
import org.dishch.tabletopaiassistant.feature.settings.domain.repository.SettingsRepository

class ClearHistoryUseCase @Inject constructor(
    private val repository: SettingsRepository,
) {

    suspend operator fun invoke(): Result<Unit> = try {
        repository.clearHistory()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(SettingsError.UnknownError(e))
    }
}
