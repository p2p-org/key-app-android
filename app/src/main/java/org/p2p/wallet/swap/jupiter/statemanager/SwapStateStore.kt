package org.p2p.wallet.swap.jupiter.statemanager

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest

class SwapStateStore(private val reducers: Set<SwapStateReducer>) {
    private val state = MutableStateFlow<SwapState>(SwapState.InitialLoading)

    fun observe(): StateFlow<SwapState> = state

    suspend fun dispatchAction(action: SwapStateAction) {
        val currentState = state.value

        val reducedStates: Flow<SwapState> =
            reducers.firstOrNull { it.canReduce(currentState) }
                ?.reduceNewState(currentState, action)
                ?: return

        reducedStates.collectLatest(state::emit)
    }
}
