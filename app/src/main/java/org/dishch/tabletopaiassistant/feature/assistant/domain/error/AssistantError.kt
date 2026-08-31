package org.dishch.tabletopaiassistant.feature.assistant.domain.error

sealed class AssistantError : Exception() {
    data object InvalidApiKey : AssistantError()
    data object Timeout : AssistantError()
    data class NetworkError(override val cause: Throwable) : AssistantError()
    data class UnknownError(override val cause: Throwable) : AssistantError()
}
