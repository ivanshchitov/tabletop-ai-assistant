package org.dishch.tabletopaiassistant.feature.settings.presentation.ui

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import org.dishch.tabletopaiassistant.feature.settings.presentation.mvi.SettingsSideEffect
import org.dishch.tabletopaiassistant.feature.settings.presentation.mvi.SettingsViewEvent
import org.dishch.tabletopaiassistant.feature.settings.presentation.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.onEvent(SettingsViewEvent.LoadSettings)
        viewModel.effects.collect { effect ->
            when (effect) {
                is SettingsSideEffect.ShowToast ->
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()

                SettingsSideEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    SettingsView(
        state = state,
        onEvent = viewModel::onEvent,
    )
}
