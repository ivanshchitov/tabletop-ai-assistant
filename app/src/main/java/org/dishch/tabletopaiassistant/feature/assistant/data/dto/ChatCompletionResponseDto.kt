package org.dishch.tabletopaiassistant.feature.assistant.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatCompletionResponseDto(
    @SerialName("choices") val choices: List<ChoiceDto>,
)
