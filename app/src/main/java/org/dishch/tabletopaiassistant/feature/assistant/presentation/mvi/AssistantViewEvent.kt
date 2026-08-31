package org.dishch.tabletopaiassistant.feature.assistant.presentation.mvi

sealed interface AssistantViewEvent {
    data class InputChanged(val text: String) : AssistantViewEvent
    data object SendQuestion : AssistantViewEvent
    data object Retry : AssistantViewEvent
    data object LoadHistory : AssistantViewEvent
    data object OpenSettings : AssistantViewEvent
}
