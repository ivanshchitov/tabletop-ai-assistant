package org.dishch.tabletopaiassistant.feature.settings.presentation

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dishch.tabletopaiassistant.R
import org.dishch.tabletopaiassistant.core.resources.ResourceProvider
import org.dishch.tabletopaiassistant.feature.settings.domain.usecase.ClearHistoryUseCase
import org.dishch.tabletopaiassistant.feature.settings.domain.usecase.ObserveDialogCountUseCase
import org.dishch.tabletopaiassistant.feature.settings.domain.usecase.ObserveSessionCountUseCase
import org.dishch.tabletopaiassistant.feature.settings.presentation.mvi.SettingsSideEffect
import org.dishch.tabletopaiassistant.feature.settings.presentation.mvi.SettingsViewEvent
import org.dishch.tabletopaiassistant.feature.settings.presentation.viewmodel.SettingsViewModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val observeDialogCountUseCase = mockk<ObserveDialogCountUseCase>()
    private val observeSessionCountUseCase = mockk<ObserveSessionCountUseCase>()
    private val clearHistoryUseCase = mockk<ClearHistoryUseCase>()
    private val resourceProvider = mockk<ResourceProvider>()

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        every { observeDialogCountUseCase() } returns Result.success(flowOf(12))
        every { observeSessionCountUseCase() } returns Result.success(flowOf(3))
        every { resourceProvider.getString(R.string.toast_history_cleared) } returns "История очищена."
        every {
            resourceProvider.getString(R.string.toast_history_clear_failed)
        } returns "Не удалось очистить историю."

        viewModel = SettingsViewModel(
            observeDialogCountUseCase = observeDialogCountUseCase,
            observeSessionCountUseCase = observeSessionCountUseCase,
            clearHistoryUseCase = clearHistoryUseCase,
            resourceProvider = resourceProvider,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `LoadSettings populates counters from the use cases`() = runTest(testDispatcher) {
        viewModel.onEvent(SettingsViewEvent.LoadSettings)
        advanceUntilIdle()

        assertEquals(12, viewModel.state.value.historyCount)
        assertEquals(3, viewModel.state.value.sessionCount)
    }

    @Test
    fun `ClearHistory delegates to the use case`() = runTest(testDispatcher) {
        coEvery { clearHistoryUseCase() } returns Result.success(Unit)

        viewModel.onEvent(SettingsViewEvent.ClearHistory)
        advanceUntilIdle()

        coVerify { clearHistoryUseCase() }
    }

    @Test
    fun `NavigateBack sends a NavigateBack effect`() = runTest(testDispatcher) {
        viewModel.onEvent(SettingsViewEvent.NavigateBack)

        assertEquals(SettingsSideEffect.NavigateBack, viewModel.effects.first())
    }
}
