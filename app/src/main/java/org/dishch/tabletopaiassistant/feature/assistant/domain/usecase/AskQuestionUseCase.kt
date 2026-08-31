package org.dishch.tabletopaiassistant.feature.assistant.domain.usecase

import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject
import org.dishch.tabletopaiassistant.feature.assistant.domain.error.AssistantError
import org.dishch.tabletopaiassistant.feature.assistant.domain.model.ChatMessage
import org.dishch.tabletopaiassistant.feature.assistant.domain.model.ChatRole
import org.dishch.tabletopaiassistant.feature.assistant.domain.repository.AssistantRepository
import retrofit2.HttpException

/**
 * Saves the user's question and fetches the assistant's answer. The answer itself is
 * intentionally NOT persisted here — the caller reveals it with a typing animation first
 * and persists it via [SaveAssistantAnswerUseCase] once the animation finishes, so the
 * Room-backed history never jumps straight to the final text.
 */
class AskQuestionUseCase @Inject constructor(
    private val repository: AssistantRepository,
) {

    suspend operator fun invoke(question: String): Result<String> {
        return try {
            repository.saveMessage(
                ChatMessage(
                    id = 0L,
                    role = ChatRole.USER,
                    content = question,
                    createdAt = System.currentTimeMillis(),
                ),
            )
            val answer = repository.askQuestion(question)
            Result.success(answer)
        } catch (e: HttpException) {
            if (e.code() == 401) {
                Result.failure(AssistantError.InvalidApiKey)
            } else {
                Result.failure(AssistantError.UnknownError(e))
            }
        } catch (e: SocketTimeoutException) {
            Result.failure(AssistantError.Timeout)
        } catch (e: IOException) {
            Result.failure(AssistantError.NetworkError(e))
        } catch (e: Exception) {
            Result.failure(AssistantError.UnknownError(e))
        }
    }
}
