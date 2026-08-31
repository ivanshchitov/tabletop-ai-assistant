package org.dishch.tabletopaiassistant.feature.settings.domain.usecase

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.dishch.tabletopaiassistant.core.session.SessionStatsHolder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ObserveSessionCountUseCaseTest {

    private val sessionStatsHolder = mockk<SessionStatsHolder>()
    private val useCase = ObserveSessionCountUseCase(sessionStatsHolder)

    @Test
    fun `invoke returns the holder's session count flow`() = runTest {
        every { sessionStatsHolder.sessionDialogCount } returns MutableStateFlow(5)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(5, result.getOrThrow().first())
    }
}
