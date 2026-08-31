package org.dishch.tabletopaiassistant.feature.assistant.domain.usecase

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import org.dishch.tabletopaiassistant.core.session.SessionStatsHolder

class ObserveSessionCountUseCase @Inject constructor(
    private val sessionStatsHolder: SessionStatsHolder,
) {

    operator fun invoke(): Result<Flow<Int>> = Result.success(sessionStatsHolder.sessionDialogCount)
}
