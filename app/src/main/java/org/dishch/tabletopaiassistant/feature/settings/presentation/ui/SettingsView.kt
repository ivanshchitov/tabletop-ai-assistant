package org.dishch.tabletopaiassistant.feature.settings.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.dishch.tabletopaiassistant.R
import org.dishch.tabletopaiassistant.core.ui.theme.TabletopAiAssistantTheme
import org.dishch.tabletopaiassistant.feature.settings.presentation.mvi.SettingsState
import org.dishch.tabletopaiassistant.feature.settings.presentation.mvi.SettingsViewEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(
    state: SettingsState,
    onEvent: (SettingsViewEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = { onEvent(SettingsViewEvent.NavigateBack) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(stringResource(R.string.settings_history_count_label))
                        Text(
                            text = state.historyCount.toString(),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(stringResource(R.string.settings_session_count_label))
                        Text(
                            text = state.sessionCount.toString(),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = { onEvent(SettingsViewEvent.ClearHistory) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text(stringResource(R.string.action_clear_history))
            }

            OutlinedButton(
                onClick = { onEvent(SettingsViewEvent.NavigateBack) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.action_back))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsViewPreview() {
    TabletopAiAssistantTheme {
        SettingsView(
            state = SettingsState(
                historyCount = 12,
                sessionCount = 3,
            ),
            onEvent = {},
        )
    }
}
