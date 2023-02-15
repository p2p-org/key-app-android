package org.p2p.wallet.swap.jupiter.statemanager

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest

class SwapStateManager(private val handlers: Set<SwapStateHandler>) {
    private val state = MutableStateFlow<SwapState>(SwapState.InitialLoading)

    fun observe(): StateFlow<SwapState> = state

    suspend fun onAction(action: SwapStateAction) {
        val currentState = state.value

        val newStates: Flow<SwapState> =
            handlers.firstOrNull { it.canHandle(currentState) }
                ?.handle(currentState, action)
                ?: return

        newStates.collectLatest(state::emit)
    }
}
