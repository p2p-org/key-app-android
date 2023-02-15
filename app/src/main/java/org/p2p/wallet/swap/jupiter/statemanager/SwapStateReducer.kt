package org.p2p.wallet.swap.jupiter.statemanager

import kotlinx.coroutines.flow.Flow

interface SwapStateReducer {
    fun canReduce(oldState: SwapState): Boolean
    fun reduceNewState(oldState: SwapState, action: SwapStateAction): Flow<SwapState>
}
