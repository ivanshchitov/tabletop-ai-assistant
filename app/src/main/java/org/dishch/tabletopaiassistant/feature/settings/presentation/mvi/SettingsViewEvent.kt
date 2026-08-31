package org.dishch.tabletopaiassistant.feature.settings.presentation.mvi

sealed interface SettingsViewEvent {
    data object LoadSettings : SettingsViewEvent
    data object ClearHistory : SettingsViewEvent
    data object NavigateBack : SettingsViewEvent
}
