package org.dishch.tabletopaiassistant.feature.assistant.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatCompletionRequestDto(
    @SerialName("model") val model: String,
    @SerialName("messages") val messages: List<MessageDto>,
    @SerialName("temperature") val temperature: Double,
    @SerialName("max_tokens") val maxTokens: Int,
)
