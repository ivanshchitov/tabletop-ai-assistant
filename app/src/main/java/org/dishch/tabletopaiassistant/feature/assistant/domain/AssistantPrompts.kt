package org.dishch.tabletopaiassistant.feature.assistant.domain

/**
 * The fixed LLM system prompt (role definition + topic filtering) lives in
 * `app/src/main/assets/system_prompt.md` and is loaded via `SystemPromptProvider` —
 * never shown on screen or editable by the user.
 */
object AssistantPrompts {

    fun buildUserPrompt(question: String): String = "Вопрос пользователя: $question"
}
