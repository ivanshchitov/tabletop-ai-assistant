package org.dishch.tabletopaiassistant.feature.assistant.domain.usecase

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.dishch.tabletopaiassistant.feature.assistant.domain.model.ChatMessage
import org.dishch.tabletopaiassistant.feature.assistant.domain.model.ChatRole
import org.dishch.tabletopaiassistant.feature.assistant.domain.repository.AssistantRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ObserveHistoryUseCaseTest {

    private val repository = mockk<AssistantRepository>()
    private val useCase = ObserveHistoryUseCase(repository)

    @Test
    fun `invoke returns the repository's history flow`() = runTest {
        val history = listOf(ChatMessage(1L, ChatRole.USER, "Правила Каркассона?", 0L))
        every { repository.observeHistory() } returns flowOf(history)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(history, result.getOrThrow().first())
    }
}
