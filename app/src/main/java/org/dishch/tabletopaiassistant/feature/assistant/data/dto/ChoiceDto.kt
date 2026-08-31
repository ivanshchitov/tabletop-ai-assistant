package org.dishch.tabletopaiassistant.feature.assistant.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChoiceDto(
    @SerialName("message") val message: MessageDto,
)
