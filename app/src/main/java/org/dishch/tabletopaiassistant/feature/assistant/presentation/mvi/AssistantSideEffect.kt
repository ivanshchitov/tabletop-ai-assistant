package org.dishch.tabletopaiassistant.feature.assistant.presentation.mvi

sealed interface AssistantSideEffect {
    data class ShowToast(val message: String) : AssistantSideEffect
    data object NavigateToSettings : AssistantSideEffect
}
