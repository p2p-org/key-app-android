package org.p2p.wallet.swap.jupiter.statemanager

import kotlinx.coroutines.flow.MutableStateFlow

interface SwapStateHandler {
    fun canHandle(oldState: SwapState): Boolean
    suspend fun handleAction(state: MutableStateFlow<SwapState>, action: SwapStateAction)
}
