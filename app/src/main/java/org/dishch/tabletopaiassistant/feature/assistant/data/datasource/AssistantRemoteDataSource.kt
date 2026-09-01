package org.dishch.tabletopaiassistant.feature.assistant.data.datasource

import javax.inject.Inject
import org.dishch.tabletopaiassistant.core.network.ApiConfig
import org.dishch.tabletopaiassistant.core.prompt.SystemPromptProvider
import org.dishch.tabletopaiassistant.feature.assistant.data.dto.ChatCompletionRequestDto
import org.dishch.tabletopaiassistant.feature.assistant.data.dto.ChatCompletionResponseDto
import org.dishch.tabletopaiassistant.feature.assistant.data.dto.MessageDto
import org.dishch.tabletopaiassistant.feature.assistant.domain.AssistantPrompts

class AssistantRemoteDataSource @Inject constructor(
    private val api: AssistantApi,
    private val systemPromptProvider: SystemPromptProvider,
) {

    suspend fun askQuestion(question: String): ChatCompletionResponseDto {
        val request = ChatCompletionRequestDto(
            model = ApiConfig.MODEL,
            messages = listOf(
                MessageDto(
                    role = "system",
                    content = systemPromptProvider.getSystemPrompt(),
                ),
                MessageDto(
                    role = "user",
                    content = AssistantPrompts.buildUserPrompt(question),
                ),
            ),
            temperature = ApiConfig.TEMPERATURE,
            maxTokens = ApiConfig.MAX_TOKENS,
        )
        return api.postChatCompletion(authorization = "Bearer ${ApiConfig.API_KEY}", request = request)
    }
}
