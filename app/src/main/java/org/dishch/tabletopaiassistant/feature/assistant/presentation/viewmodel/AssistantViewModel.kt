package org.dishch.tabletopaiassistant.feature.assistant.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.dishch.tabletopaiassistant.R
import org.dishch.tabletopaiassistant.core.resources.ResourceProvider
import org.dishch.tabletopaiassistant.feature.assistant.domain.error.AssistantError
import org.dishch.tabletopaiassistant.feature.assistant.domain.usecase.AskQuestionUseCase
import org.dishch.tabletopaiassistant.feature.assistant.domain.usecase.IncrementSessionCountUseCase
import org.dishch.tabletopaiassistant.feature.assistant.domain.usecase.ObserveHistoryUseCase
import org.dishch.tabletopaiassistant.feature.assistant.domain.usecase.ObserveSessionCountUseCase
import org.dishch.tabletopaiassistant.feature.assistant.domain.usecase.SaveAssistantAnswerUseCase
import org.dishch.tabletopaiassistant.feature.assistant.presentation.mvi.AssistantSideEffect
import org.dishch.tabletopaiassistant.feature.assistant.presentation.mvi.AssistantState
import org.dishch.tabletopaiassistant.feature.assistant.presentation.mvi.AssistantViewEvent

@HiltViewModel
class AssistantViewModel @Inject constructor(
    private val askQuestionUseCase: AskQuestionUseCase,
    private val saveAssistantAnswerUseCase: SaveAssistantAnswerUseCase,
    private val observeHistoryUseCase: ObserveHistoryUseCase,
    private val observeSessionCountUseCase: ObserveSessionCountUseCase,
    private val incrementSessionCountUseCase: IncrementSessionCountUseCase,
    private val resourceProvider: ResourceProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(AssistantState())
    val state: StateFlow<AssistantState> = _state.asStateFlow()

    private val _effects = Channel<AssistantSideEffect>(Channel.BUFFERED)
    val effects: Flow<AssistantSideEffect> = _effects.receiveAsFlow()

    private var historyLoaded = false
    private var lastQuestion: String = ""

    fun onEvent(event: AssistantViewEvent) {
        when (event) {
            is AssistantViewEvent.InputChanged -> onInputChanged(event.text)
            AssistantViewEvent.SendQuestion -> onSendQuestion()
            AssistantViewEvent.Retry -> onRetry()
            AssistantViewEvent.LoadHistory -> loadHistory()
            AssistantViewEvent.OpenSettings -> sendEffect(AssistantSideEffect.NavigateToSettings)
        }
    }

    private fun loadHistory() {
        if (historyLoaded) return
        historyLoaded = true

        observeHistoryUseCase().onSuccess { historyFlow ->
            viewModelScope.launch {
                historyFlow.collect { messages ->
                    _state.update { it.copy(messages = messages) }
                }
            }
        }

        observeSessionCountUseCase().onSuccess { countFlow ->
            viewModelScope.launch {
                countFlow.collect { count ->
                    _state.update { it.copy(sessionCount = count) }
                }
            }
        }
    }

    private fun onInputChanged(text: String) {
        if (text.length > MAX_INPUT_LENGTH) {
            _state.update { it.copy(input = text.take(MAX_INPUT_LENGTH)) }
            sendEffect(
                AssistantSideEffect.ShowToast(
                    resourceProvider.getString(R.string.toast_input_truncated, MAX_INPUT_LENGTH),
                ),
            )
        } else {
            _state.update { it.copy(input = text) }
        }
    }

    private fun onSendQuestion() {
        val question = _state.value.input.trim()
        if (question.isBlank() || _state.value.isSending) return

        lastQuestion = question
        _state.update { it.copy(input = "", isSending = true, errorMessage = null) }
        submitQuestion(question)
    }

    private fun onRetry() {
        if (lastQuestion.isBlank() || _state.value.isSending) return
        _state.update { it.copy(isSending = true, errorMessage = null) }
        submitQuestion(lastQuestion)
    }

    private fun submitQuestion(question: String) {
        viewModelScope.launch {
            val result = askQuestionUseCase(question)
            result.onSuccess { answer ->
                _state.update { it.copy(isSending = false) }
                revealAnswer(answer)
            }
            result.onFailure { error ->
                _state.update { it.copy(isSending = false, errorMessage = mapError(error)) }
            }
        }
    }

    private suspend fun revealAnswer(answer: String) {
        _state.update { it.copy(isTyping = true, typingContent = "") }

        val revealed = StringBuilder()
        answer.chunked(TYPING_CHUNK_SIZE).forEach { chunk ->
            revealed.append(chunk)
            _state.update { it.copy(typingContent = revealed.toString()) }
            delay(TYPING_DELAY_MS)
        }

        _state.update { it.copy(isTyping = false, typingContent = "") }
        saveAssistantAnswerUseCase(answer)
        incrementSessionCountUseCase()
    }

    private fun mapError(error: Throwable): String = when (error) {
        AssistantError.InvalidApiKey -> resourceProvider.getString(R.string.error_invalid_api_key)
        AssistantError.Timeout -> resourceProvider.getString(R.string.error_timeout)
        is AssistantError.NetworkError -> resourceProvider.getString(R.string.error_network)
        else -> resourceProvider.getString(R.string.error_unknown)
    }

    private fun sendEffect(effect: AssistantSideEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }

    private companion object {
        const val MAX_INPUT_LENGTH = 2000
        const val TYPING_CHUNK_SIZE = 3
        const val TYPING_DELAY_MS = 15L
    }
}
