package org.dishch.tabletopaiassistant.feature.assistant.presentation

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dishch.tabletopaiassistant.R
import org.dishch.tabletopaiassistant.core.resources.ResourceProvider
import org.dishch.tabletopaiassistant.feature.assistant.domain.error.AssistantError
import org.dishch.tabletopaiassistant.feature.assistant.domain.model.ChatMessage
import org.dishch.tabletopaiassistant.feature.assistant.domain.usecase.AskQuestionUseCase
import org.dishch.tabletopaiassistant.feature.assistant.domain.usecase.IncrementSessionCountUseCase
import org.dishch.tabletopaiassistant.feature.assistant.domain.usecase.ObserveHistoryUseCase
import org.dishch.tabletopaiassistant.feature.assistant.domain.usecase.ObserveSessionCountUseCase
import org.dishch.tabletopaiassistant.feature.assistant.domain.usecase.SaveAssistantAnswerUseCase
import org.dishch.tabletopaiassistant.feature.assistant.presentation.mvi.AssistantViewEvent
import org.dishch.tabletopaiassistant.feature.assistant.presentation.viewmodel.AssistantViewModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AssistantViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val askQuestionUseCase = mockk<AskQuestionUseCase>()
    private val saveAssistantAnswerUseCase = mockk<SaveAssistantAnswerUseCase>()
    private val observeHistoryUseCase = mockk<ObserveHistoryUseCase>()
    private val observeSessionCountUseCase = mockk<ObserveSessionCountUseCase>()
    private val incrementSessionCountUseCase = mockk<IncrementSessionCountUseCase>()
    private val resourceProvider = mockk<ResourceProvider>()

    private lateinit var viewModel: AssistantViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        every { observeHistoryUseCase() } returns Result.success(flowOf(emptyList<ChatMessage>()))
        every { observeSessionCountUseCase() } returns Result.success(flowOf(0))
        every { incrementSessionCountUseCase() } returns Result.success(Unit)
        every { resourceProvider.getString(R.string.error_timeout) } returns "Превышено время ожидания ответа."

        viewModel = AssistantViewModel(
            askQuestionUseCase = askQuestionUseCase,
            saveAssistantAnswerUseCase = saveAssistantAnswerUseCase,
            observeHistoryUseCase = observeHistoryUseCase,
            observeSessionCountUseCase = observeSessionCountUseCase,
            incrementSessionCountUseCase = incrementSessionCountUseCase,
            resourceProvider = resourceProvider,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `SendQuestion sets isSending then reveals the answer on success`() = runTest(testDispatcher) {
        viewModel.onEvent(AssistantViewEvent.LoadHistory)
        coEvery { askQuestionUseCase("Правила Каркассона?") } returns Result.success("Ответ")
        coEvery { saveAssistantAnswerUseCase("Ответ") } returns Result.success(Unit)

        viewModel.onEvent(AssistantViewEvent.InputChanged("Правила Каркассона?"))
        viewModel.onEvent(AssistantViewEvent.SendQuestion)
        assertEquals(true, viewModel.state.value.isSending)

        advanceUntilIdle()

        assertFalse(viewModel.state.value.isSending)
        coVerify { saveAssistantAnswerUseCase("Ответ") }
        verify { incrementSessionCountUseCase() }
    }

    @Test
    fun `SendQuestion sets errorMessage on failure`() = runTest(testDispatcher) {
        viewModel.onEvent(AssistantViewEvent.LoadHistory)
        coEvery { askQuestionUseCase(any()) } returns Result.failure(AssistantError.Timeout)

        viewModel.onEvent(AssistantViewEvent.InputChanged("Вопрос про Монополию"))
        viewModel.onEvent(AssistantViewEvent.SendQuestion)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isSending)
        assertEquals("Превышено время ожидания ответа.", viewModel.state.value.errorMessage)
    }
}
