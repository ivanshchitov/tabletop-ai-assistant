package org.dishch.tabletopaiassistant.feature.assistant.domain.usecase

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import org.dishch.tabletopaiassistant.feature.assistant.domain.model.ChatMessage
import org.dishch.tabletopaiassistant.feature.assistant.domain.repository.AssistantRepository

class ObserveHistoryUseCase @Inject constructor(
    private val repository: AssistantRepository,
) {

    operator fun invoke(): Result<Flow<List<ChatMessage>>> =
        Result.success(repository.observeHistory())
}
