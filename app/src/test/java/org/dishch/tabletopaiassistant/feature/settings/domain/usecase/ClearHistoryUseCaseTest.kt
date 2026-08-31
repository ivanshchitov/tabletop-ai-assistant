package org.dishch.tabletopaiassistant.feature.settings.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.dishch.tabletopaiassistant.feature.settings.domain.error.SettingsError
import org.dishch.tabletopaiassistant.feature.settings.domain.repository.SettingsRepository
import org.junit.Assert.assertTrue
import org.junit.Test

class ClearHistoryUseCaseTest {

    private val repository = mockk<SettingsRepository>()
    private val useCase = ClearHistoryUseCase(repository)

    @Test
    fun `invoke returns success when repository clears history`() = runTest {
        coEvery { repository.clearHistory() } returns Unit

        val result = useCase()

        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke returns UnknownError when repository throws`() = runTest {
        coEvery { repository.clearHistory() } throws IllegalStateException("db error")

        val result = useCase()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is SettingsError.UnknownError)
    }
}
