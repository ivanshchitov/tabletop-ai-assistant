package org.dishch.tabletopaiassistant.feature.assistant.data.datasource

import org.dishch.tabletopaiassistant.feature.assistant.data.dto.ChatCompletionRequestDto
import org.dishch.tabletopaiassistant.feature.assistant.data.dto.ChatCompletionResponseDto
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AssistantApi {

    @POST("chat/completions")
    suspend fun postChatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: ChatCompletionRequestDto,
    ): ChatCompletionResponseDto
}
