package org.dishch.tabletopaiassistant.feature.assistant.domain.usecase

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.dishch.tabletopaiassistant.feature.assistant.domain.error.AssistantError
import org.dishch.tabletopaiassistant.feature.assistant.domain.model.ChatRole
import org.dishch.tabletopaiassistant.feature.assistant.domain.repository.AssistantRepository
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveAssistantAnswerUseCaseTest {

    private val repository = mockk<AssistantRepository>()
    private val useCase = SaveAssistantAnswerUseCase(repository)

    @Test
    fun `invoke saves the answer as an assistant message`() = runTest {
        coEvery { repository.saveMessage(any()) } just Runs

        val result = useCase("Ответ")

        assertTrue(result.isSuccess)
        coVerify { repository.saveMessage(match { it.role == ChatRole.ASSISTANT && it.content == "Ответ" }) }
    }

    @Test
    fun `invoke returns UnknownError when repository throws`() = runTest {
        coEvery { repository.saveMessage(any()) } throws IllegalStateException("db error")

        val result = useCase("Ответ")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AssistantError.UnknownError)
    }
}
