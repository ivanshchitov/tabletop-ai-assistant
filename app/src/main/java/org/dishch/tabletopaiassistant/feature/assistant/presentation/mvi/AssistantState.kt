package org.dishch.tabletopaiassistant.feature.assistant.presentation.mvi

import org.dishch.tabletopaiassistant.feature.assistant.domain.model.ChatMessage

data class AssistantState(
    val messages: List<ChatMessage> = emptyList(),
    val input: String = "",
    val isSending: Boolean = false,
    val isTyping: Boolean = false,
    val typingContent: String = "",
    val errorMessage: String? = null,
    val sessionCount: Int = 0,
)
