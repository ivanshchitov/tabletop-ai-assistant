package org.dishch.tabletopaiassistant.feature.settings.presentation.mvi

sealed interface SettingsSideEffect {
    data class ShowToast(val message: String) : SettingsSideEffect
    data object NavigateBack : SettingsSideEffect
}
