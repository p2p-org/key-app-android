package org.p2p.wallet.swap.jupiter.statemanager

import kotlinx.coroutines.flow.MutableStateFlow

private const val DEFAULT_ACTIVE_ROUTE_ORDINAL = 1

class SwapStateTokenAZeroHandler(
    private val swapRoutesRefresher: SwapStateRoutesRefresher
) : SwapStateHandler {

    override fun canHandle(state: SwapState): Boolean = state is SwapState.TokenAZero

    override suspend fun handleAction(state: MutableStateFlow<SwapState>, action: SwapStateAction) {
        val oldState = state.value
        oldState as SwapState.TokenAZero

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
                    activeRouteOrdinal = DEFAULT_ACTIVE_ROUTE_ORDINAL
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
