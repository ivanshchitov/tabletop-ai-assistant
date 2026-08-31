package org.dishch.tabletopaiassistant.feature.assistant.data.mapper

import org.dishch.tabletopaiassistant.core.database.entity.ChatMessageEntity
import org.dishch.tabletopaiassistant.feature.assistant.data.dto.ChatCompletionResponseDto
import org.dishch.tabletopaiassistant.feature.assistant.domain.model.ChatMessage
import org.dishch.tabletopaiassistant.feature.assistant.domain.model.ChatRole

fun ChatMessageEntity.toDomain(): ChatMessage = ChatMessage(
    id = id,
    role = ChatRole.valueOf(role.uppercase()),
    content = content,
    createdAt = createdAt,
)

fun ChatMessage.toEntity(): ChatMessageEntity = ChatMessageEntity(
    id = id,
    role = role.name.lowercase(),
    content = content,
    createdAt = createdAt,
)

fun ChatCompletionResponseDto.toAnswerText(): String =
    choices.firstOrNull()?.message?.content.orEmpty()
