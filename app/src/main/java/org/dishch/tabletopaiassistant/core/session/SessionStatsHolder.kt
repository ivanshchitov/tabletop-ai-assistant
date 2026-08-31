package org.dishch.tabletopaiassistant.core.session

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * In-memory count of dialogs (question+answer exchanges) completed since the process started.
 * Read by both the assistant feature (chat status bar) and the settings feature (counter), and
 * written by the assistant feature — living in `core/` is what lets those two features share it
 * without depending on one another.
 */
@Singleton
class SessionStatsHolder @Inject constructor() {

    private val _sessionDialogCount = MutableStateFlow(0)
    val sessionDialogCount: StateFlow<Int> = _sessionDialogCount.asStateFlow()

    fun incrementSessionDialogCount() {
        _sessionDialogCount.update { it + 1 }
    }
}
