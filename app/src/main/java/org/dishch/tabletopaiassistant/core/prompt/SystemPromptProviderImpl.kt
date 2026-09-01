package org.dishch.tabletopaiassistant.core.prompt

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val SYSTEM_PROMPT_ASSET = "system_prompt.md"

class SystemPromptProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : SystemPromptProvider {

    private val cachedSystemPrompt: String by lazy {
        context.assets.open(SYSTEM_PROMPT_ASSET).bufferedReader().use { it.readText() }.trim()
    }

    override fun getSystemPrompt(): String = cachedSystemPrompt
}
