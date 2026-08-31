package org.dishch.tabletopaiassistant.feature.assistant.presentation.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.jeziellago.compose.markdowntext.MarkdownText
import org.dishch.tabletopaiassistant.core.ui.theme.AssistantAccentPurple
import org.dishch.tabletopaiassistant.core.ui.theme.SystemMessageYellow
import org.dishch.tabletopaiassistant.core.ui.theme.UserBubbleBlue
import org.dishch.tabletopaiassistant.feature.assistant.domain.model.ChatRole

@Composable
fun MessageBubble(
    role: ChatRole,
    content: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
) {
    val isUser = role == ChatRole.USER
    val accentColor = when {
        isError -> MaterialTheme.colorScheme.error
        role == ChatRole.USER -> UserBubbleBlue
        role == ChatRole.ASSISTANT -> AssistantAccentPurple
        else -> SystemMessageYellow
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Surface(
            color = accentColor.copy(alpha = 0.14f),
            contentColor = accentColor,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.widthIn(max = 320.dp),
        ) {
            if (isUser) {
                Text(
                    text = content,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    textAlign = TextAlign.Start,
                )
            } else {
                MarkdownText(
                    markdown = content,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}
