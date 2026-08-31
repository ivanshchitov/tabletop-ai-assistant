package org.dishch.tabletopaiassistant.feature.settings.domain.error

sealed class SettingsError : Exception() {
    data class UnknownError(override val cause: Throwable) : SettingsError()
}
