package org.dishch.tabletopaiassistant.feature.settings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.dishch.tabletopaiassistant.R
import org.dishch.tabletopaiassistant.core.resources.ResourceProvider
import org.dishch.tabletopaiassistant.feature.settings.domain.usecase.ClearHistoryUseCase
import org.dishch.tabletopaiassistant.feature.settings.domain.usecase.ObserveDialogCountUseCase
import org.dishch.tabletopaiassistant.feature.settings.domain.usecase.ObserveSessionCountUseCase
import org.dishch.tabletopaiassistant.feature.settings.presentation.mvi.SettingsSideEffect
import org.dishch.tabletopaiassistant.feature.settings.presentation.mvi.SettingsState
import org.dishch.tabletopaiassistant.feature.settings.presentation.mvi.SettingsViewEvent

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val observeDialogCountUseCase: ObserveDialogCountUseCase,
    private val observeSessionCountUseCase: ObserveSessionCountUseCase,
    private val clearHistoryUseCase: ClearHistoryUseCase,
    private val resourceProvider: ResourceProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private val _effects = Channel<SettingsSideEffect>(Channel.BUFFERED)
    val effects: Flow<SettingsSideEffect> = _effects.receiveAsFlow()

    private var settingsLoaded = false

    fun onEvent(event: SettingsViewEvent) {
        when (event) {
            SettingsViewEvent.LoadSettings -> loadSettings()
            SettingsViewEvent.ClearHistory -> onClearHistory()
            SettingsViewEvent.NavigateBack -> sendEffect(SettingsSideEffect.NavigateBack)
        }
    }

    private fun loadSettings() {
        if (settingsLoaded) return
        settingsLoaded = true

        observeDialogCountUseCase().onSuccess { countFlow ->
            viewModelScope.launch {
                countFlow.collect { count ->
                    _state.update { it.copy(historyCount = count) }
                }
            }
        }

        observeSessionCountUseCase().onSuccess { countFlow ->
            viewModelScope.launch {
                countFlow.collect { count ->
                    _state.update { it.copy(sessionCount = count) }
                }
            }
        }
    }

    private fun onClearHistory() {
        viewModelScope.launch {
            clearHistoryUseCase()
                .onSuccess {
                    sendEffect(SettingsSideEffect.ShowToast(resourceProvider.getString(R.string.toast_history_cleared)))
                }
                .onFailure {
                    sendEffect(
                        SettingsSideEffect.ShowToast(resourceProvider.getString(R.string.toast_history_clear_failed)),
                    )
                }
        }
    }

    private fun sendEffect(effect: SettingsSideEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}
