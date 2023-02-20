package org.p2p.wallet.swap.jupiter.statemanager

import kotlinx.coroutines.flow.MutableStateFlow

class SwapStateTokenAZeroHandler(
    private val swapRoutesRefresher: SwapStateRoutesRefresher
) : SwapStateHandler {

    override fun canHandle(oldState: SwapState): Boolean = oldState is SwapState.TokenAZero

    override suspend fun handleAction(state: MutableStateFlow<SwapState>, action: SwapStateAction) {
        val oldState = state.value as SwapState.TokenAZero

        when (action) {
            is SwapStateAction.SlippageChanged -> {
                state.value = oldState.copy(slippage = action.newSlippageValue)
            }
            is SwapStateAction.TokenAAmountChanged -> {
                swapRoutesRefresher.refreshRoutes(
                    state = state,
                    tokenA = oldState.tokenA,
                    tokenB = oldState.tokenB,
                    amountTokenA = action.newAmount,
                    slippage = oldState.slippage,
                    activeRouteOrdinal = 1
                )
            }
            is SwapStateAction.TokenAChanged -> {
                state.value = oldState.copy(tokenA = action.newTokenA)
            }
            is SwapStateAction.TokenBChanged -> {
                state.value = oldState.copy(tokenB = action.newTokenB)
            }
            else -> Unit
        }
    }
}
