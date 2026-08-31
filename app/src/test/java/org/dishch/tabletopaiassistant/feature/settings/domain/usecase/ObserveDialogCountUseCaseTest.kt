package org.dishch.tabletopaiassistant.feature.settings.domain.usecase

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.dishch.tabletopaiassistant.feature.settings.domain.repository.SettingsRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ObserveDialogCountUseCaseTest {

    private val repository = mockk<SettingsRepository>()
    private val useCase = ObserveDialogCountUseCase(repository)

    @Test
    fun `invoke returns the repository's dialog count flow`() = runTest {
        every { repository.observeDialogCount() } returns flowOf(12)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(12, result.getOrThrow().first())
    }
}
