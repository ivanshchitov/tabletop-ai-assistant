package org.dishch.tabletopaiassistant.feature.assistant.domain.usecase

import javax.inject.Inject
import org.dishch.tabletopaiassistant.core.session.SessionStatsHolder

class IncrementSessionCountUseCase @Inject constructor(
    private val sessionStatsHolder: SessionStatsHolder,
) {

    operator fun invoke(): Result<Unit> {
        sessionStatsHolder.incrementSessionDialogCount()
        return Result.success(Unit)
    }
}
