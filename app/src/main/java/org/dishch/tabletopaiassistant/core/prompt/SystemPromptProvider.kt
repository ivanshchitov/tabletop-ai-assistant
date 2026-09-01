package org.dishch.tabletopaiassistant.core.prompt

/** Loads the fixed LLM system prompt shipped as an asset file, never shown on screen or editable by the user. */
interface SystemPromptProvider {

    fun getSystemPrompt(): String
}
