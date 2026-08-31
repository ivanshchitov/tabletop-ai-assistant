package org.dishch.tabletopaiassistant.feature.settings.domain.usecase

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import org.dishch.tabletopaiassistant.feature.settings.domain.repository.SettingsRepository

class ObserveDialogCountUseCase @Inject constructor(
    private val repository: SettingsRepository,
) {

    operator fun invoke(): Result<Flow<Int>> = Result.success(repository.observeDialogCount())
}
