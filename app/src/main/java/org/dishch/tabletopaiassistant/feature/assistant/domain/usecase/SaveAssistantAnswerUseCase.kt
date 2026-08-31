package org.dishch.tabletopaiassistant.feature.assistant.domain.usecase

import javax.inject.Inject
import org.dishch.tabletopaiassistant.feature.assistant.domain.error.AssistantError
import org.dishch.tabletopaiassistant.feature.assistant.domain.model.ChatMessage
import org.dishch.tabletopaiassistant.feature.assistant.domain.model.ChatRole
import org.dishch.tabletopaiassistant.feature.assistant.domain.repository.AssistantRepository

/**
 * Persists the assistant's answer after it has finished the client-side typing
 * animation, so the message list from [ObserveHistoryUseCase] shows the final
 * text only once the animation is complete.
 */
class SaveAssistantAnswerUseCase @Inject constructor(
    private val repository: AssistantRepository,
) {

    suspend operator fun invoke(answer: String): Result<Unit> = try {
        repository.saveMessage(
            ChatMessage(
                id = 0L,
                role = ChatRole.ASSISTANT,
                content = answer,
                createdAt = System.currentTimeMillis(),
            ),
        )
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(AssistantError.UnknownError(e))
    }
}
