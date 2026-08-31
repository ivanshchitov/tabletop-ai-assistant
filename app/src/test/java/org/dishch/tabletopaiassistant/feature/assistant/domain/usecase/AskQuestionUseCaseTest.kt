package org.dishch.tabletopaiassistant.feature.assistant.domain.usecase

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import java.io.IOException
import java.net.SocketTimeoutException
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.dishch.tabletopaiassistant.feature.assistant.domain.error.AssistantError
import org.dishch.tabletopaiassistant.feature.assistant.domain.model.ChatRole
import org.dishch.tabletopaiassistant.feature.assistant.domain.repository.AssistantRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class AskQuestionUseCaseTest {

    private val repository = mockk<AssistantRepository>()
    private val useCase = AskQuestionUseCase(repository)

    @Test
    fun `invoke returns success and saves user message when api call succeeds`() = runTest {
        coEvery { repository.saveMessage(any()) } just Runs
        coEvery { repository.askQuestion("Правила Каркассона?") } returns "Ответ"

        val result = useCase("Правила Каркассона?")

        assertTrue(result.isSuccess)
        assertEquals("Ответ", result.getOrNull())
        coVerify {
            repository.saveMessage(
                match { it.role == ChatRole.USER && it.content == "Правила Каркассона?" },
            )
        }
    }

    @Test
    fun `invoke returns InvalidApiKey on 401`() = runTest {
        coEvery { repository.saveMessage(any()) } just Runs
        val response = Response.error<Any>(401, "".toResponseBody("text/plain".toMediaType()))
        coEvery { repository.askQuestion(any()) } throws HttpException(response)

        val result = useCase("Вопрос")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AssistantError.InvalidApiKey)
    }

    @Test
    fun `invoke returns Timeout on socket timeout`() = runTest {
        coEvery { repository.saveMessage(any()) } just Runs
        coEvery { repository.askQuestion(any()) } throws SocketTimeoutException()

        val result = useCase("Вопрос")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AssistantError.Timeout)
    }

    @Test
    fun `invoke returns NetworkError on IOException`() = runTest {
        coEvery { repository.saveMessage(any()) } just Runs
        coEvery { repository.askQuestion(any()) } throws IOException("no connection")

        val result = useCase("Вопрос")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AssistantError.NetworkError)
    }
}
