package org.dishch.tabletopaiassistant.feature.assistant.domain.model

data class ChatMessage(
    val id: Long,
    val role: ChatRole,
    val content: String,
    val createdAt: Long,
)
