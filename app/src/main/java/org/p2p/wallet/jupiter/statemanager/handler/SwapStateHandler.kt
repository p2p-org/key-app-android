package org.p2p.wallet.jupiter.statemanager.handler

import org.p2p.wallet.jupiter.statemanager.SwapState
import org.p2p.wallet.jupiter.statemanager.SwapStateAction
import kotlinx.coroutines.flow.MutableStateFlow

interface SwapStateHandler {
    fun canHandle(state: SwapState): Boolean
    suspend fun handleAction(
        stateFlow: MutableStateFlow<SwapState>,
        state: SwapState,
        action: SwapStateAction
    )
}
