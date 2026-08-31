package org.dishch.tabletopaiassistant.feature.assistant.presentation.ui

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import org.dishch.tabletopaiassistant.feature.assistant.presentation.mvi.AssistantSideEffect
import org.dishch.tabletopaiassistant.feature.assistant.presentation.mvi.AssistantViewEvent
import org.dishch.tabletopaiassistant.feature.assistant.presentation.viewmodel.AssistantViewModel

@Composable
fun AssistantScreen(
    viewModel: AssistantViewModel,
    onNavigateToSettings: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.onEvent(AssistantViewEvent.LoadHistory)
        viewModel.effects.collect { effect ->
            when (effect) {
                is AssistantSideEffect.ShowToast ->
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()

                AssistantSideEffect.NavigateToSettings -> onNavigateToSettings()
            }
        }
    }

    AssistantView(
        state = state,
        onEvent = viewModel::onEvent,
    )
}
