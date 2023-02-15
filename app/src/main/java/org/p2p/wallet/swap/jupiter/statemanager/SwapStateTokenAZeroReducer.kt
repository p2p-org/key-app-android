package org.p2p.wallet.swap.jupiter.statemanager

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

class SwapStateTokenAZeroReducer(
    private val swapRoutesRefresher: SwapRoutesRefresher
) : SwapStateReducer {

    override fun canReduce(oldState: SwapState): Boolean = oldState is SwapState.TokenAZero

    override fun reduceNewState(oldState: SwapState, action: SwapStateAction): Flow<SwapState> {
        oldState as SwapState.TokenAZero
        return flow {
            when (action) {
                is SwapStateAction.SlippageChanged -> {
                    emit(
                        SwapState.TokenAZero(
                            tokenA = oldState.tokenA,
                            tokenB = oldState.tokenB,
                            slippage = action.newSlippageValue
                        )
                    )
                }
                is SwapStateAction.TokenAAmountChanged -> {
                    emitAll(
                        swapRoutesRefresher.refreshRoutes(
                            tokenA = oldState.tokenA,
                            tokenB = oldState.tokenB,
                            amountTokenA = action.newAmount,
                            slippage = oldState.slippage,
                            activeRouteOrdinal = 1
                        )
                    )
                }
                is SwapStateAction.TokenAChanged -> {
                    emit(
                        SwapState.TokenAZero(
                            tokenA = action.newTokenA,
                            tokenB = oldState.tokenB,
                            slippage = oldState.slippage
                        )
                    )
                }
                is SwapStateAction.TokenBChanged -> {
                    emit(
                        SwapState.TokenAZero(
                            tokenA = oldState.tokenA,
                            tokenB = action.newTokenB,
                            slippage = oldState.slippage
                        )
                    )
                }
                else -> Unit
            }
        }
    }
}
