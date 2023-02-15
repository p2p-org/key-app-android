package org.p2p.wallet.swap.jupiter.statemanager

import kotlinx.coroutines.flow.Flow

interface SwapStateHandler {
    fun canHandle(oldState: SwapState): Boolean
    fun handle(oldState: SwapState, action: SwapStateAction): Flow<SwapState>
}
