package org.dishch.tabletopaiassistant.feature.assistant.domain.usecase

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.dishch.tabletopaiassistant.core.session.SessionStatsHolder
import org.junit.Assert.assertTrue
import org.junit.Test

class IncrementSessionCountUseCaseTest {

    private val sessionStatsHolder = mockk<SessionStatsHolder>()
    private val useCase = IncrementSessionCountUseCase(sessionStatsHolder)

    @Test
    fun `invoke increments the holder's session count`() {
        every { sessionStatsHolder.incrementSessionDialogCount() } just Runs

        val result = useCase()

        assertTrue(result.isSuccess)
        verify { sessionStatsHolder.incrementSessionDialogCount() }
    }
}
