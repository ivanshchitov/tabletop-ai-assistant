package org.dishch.tabletopaiassistant.feature.assistant.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.dishch.tabletopaiassistant.R
import org.dishch.tabletopaiassistant.core.ui.theme.TabletopAiAssistantTheme
import org.dishch.tabletopaiassistant.feature.assistant.domain.model.ChatMessage
import org.dishch.tabletopaiassistant.feature.assistant.domain.model.ChatRole
import org.dishch.tabletopaiassistant.feature.assistant.presentation.mvi.AssistantState
import org.dishch.tabletopaiassistant.feature.assistant.presentation.mvi.AssistantViewEvent
import org.dishch.tabletopaiassistant.feature.assistant.presentation.ui.component.MessageBubble
import org.dishch.tabletopaiassistant.feature.assistant.presentation.ui.component.StatusBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantView(
    state: AssistantState,
    onEvent: (AssistantViewEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val itemCount = state.messages.size + if (state.isTyping) 1 else 0

    LaunchedEffect(itemCount, state.errorMessage) {
        if (itemCount > 0) {
            listState.animateScrollToItem(itemCount - 1)
        }
    }

    Scaffold(
        modifier = modifier.imePadding(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.chat_title)) },
                actions = {
                    IconButton(onClick = { onEvent(AssistantViewEvent.OpenSettings) }) {
                        Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.action_settings))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(),
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (state.messages.isEmpty()) {
                    item(key = "welcome") {
                        MessageBubble(
                            role = ChatRole.SYSTEM,
                            content = stringResource(R.string.welcome_message),
                        )
                    }
                }

                items(items = state.messages, key = ChatMessage::id) { message ->
                    MessageBubble(role = message.role, content = message.content)
                }

                if (state.isTyping) {
                    item(key = "typing") {
                        MessageBubble(role = ChatRole.ASSISTANT, content = state.typingContent)
                    }
                }

                if (state.errorMessage != null) {
                    item(key = "error") {
                        Column {
                            MessageBubble(
                                role = ChatRole.SYSTEM,
                                content = state.errorMessage,
                                isError = true,
                            )
                            TextButton(onClick = { onEvent(AssistantViewEvent.Retry) }) {
                                Text(stringResource(R.string.action_retry))
                            }
                        }
                    }
                }
            }

            HorizontalDivider()
            StatusBar(isSending = state.isSending, sessionCount = state.sessionCount)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = state.input,
                    onValueChange = { onEvent(AssistantViewEvent.InputChanged(it)) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(R.string.input_placeholder)) },
                    maxLines = 4,
                )
                Spacer(Modifier.width(8.dp))
                if (state.isSending) {
                    CircularProgressIndicator(modifier = Modifier.height(36.dp))
                } else {
                    IconButton(onClick = { onEvent(AssistantViewEvent.SendQuestion) }) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = stringResource(R.string.action_send))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AssistantViewPreview() {
    TabletopAiAssistantTheme {
        AssistantView(
            state = AssistantState(
                messages = listOf(
                    ChatMessage(1, ChatRole.USER, "Сколько игроков в Каркассоне?", 0L),
                    ChatMessage(
                        2,
                        ChatRole.ASSISTANT,
                        "**Каркассон** рассчитан на 2–5 игроков 🎲",
                        1L,
                    ),
                ),
                sessionCount = 1,
            ),
            onEvent = {},
        )
    }
}
