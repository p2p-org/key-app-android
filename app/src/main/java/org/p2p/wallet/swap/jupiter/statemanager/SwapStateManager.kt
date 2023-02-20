package org.p2p.wallet.swap.jupiter.statemanager

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SwapStateManager(
    private val handlers: Set<SwapStateHandler>,
    private val scope: SwapCoroutineScope
) {
    private val state = MutableStateFlow<SwapState>(SwapState.InitialLoading)
    private var activeActionHandleJob: Job? = null
    private var refreshJob: Job? = null

    fun observe(): StateFlow<SwapState> = state

    fun onNewAction(action: SwapStateAction) {
        val currentState = state.value
        val actionHandler = handlers.firstOrNull { it.canHandle(currentState) } ?: return

        activeActionHandleJob = scope.launch { actionHandler.handleAction(state, action) }
    }
}
