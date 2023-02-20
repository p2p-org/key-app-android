package org.p2p.wallet.swap.jupiter.statemanager

import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SwapStateManager(
    private val handlers: Set<SwapStateHandler>,
    private val dispatchers: CoroutineDispatchers,
) : CoroutineScope {
    override val coroutineContext: CoroutineContext = SupervisorJob() + dispatchers.io
    private val state = MutableStateFlow<SwapState>(SwapState.InitialLoading)
    private var activeActionHandle: Job? = null
    private var refreshJob: Job? = null

    fun observe(): StateFlow<SwapState> = state

    fun onNewAction(action: SwapStateAction) {
        val currentState = state.value
        val actionHandler = handlers.firstOrNull { it.canHandle(currentState) } ?: return

        refreshJob?.cancel()
        activeActionHandle?.cancel()
        activeActionHandle = launch { actionHandler.handleAction(state, action) }
    }

    fun finishWork() {
        coroutineContext.cancelChildren()
    }
}
