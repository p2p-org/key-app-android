package org.p2p.wallet.jupiter.statemanager.handler

import kotlinx.coroutines.flow.MutableStateFlow
import org.p2p.wallet.jupiter.analytics.JupiterSwapMainScreenAnalytics
import org.p2p.wallet.jupiter.statemanager.SwapState
import org.p2p.wallet.jupiter.statemanager.SwapStateAction
import org.p2p.wallet.jupiter.statemanager.SwapStateManager.Companion.DEFAULT_ACTIVE_ROUTE_ORDINAL
import org.p2p.wallet.jupiter.statemanager.SwapStateRoutesRefresher
import org.p2p.wallet.jupiter.statemanager.validator.SwapValidator

class SwapStateTokenAZeroHandler(
    private val swapRoutesRefresher: SwapStateRoutesRefresher,
    private val swapValidator: SwapValidator,
    private val analytics: JupiterSwapMainScreenAnalytics
) : SwapStateHandler {

    override fun canHandle(state: SwapState): Boolean = state is SwapState.TokenAZero

    override suspend fun handleAction(
        stateFlow: MutableStateFlow<SwapState>,
        state: SwapState,
        action: SwapStateAction,
    ) {
        val oldState = state as SwapState.TokenAZero

        when (action) {
            is SwapStateAction.SlippageChanged -> {
                stateFlow.value = oldState.copy(slippage = action.newSlippageValue)
            }
            is SwapStateAction.TokenAAmountChanged -> {
                swapRoutesRefresher.refreshRoutes(
                    state = stateFlow,
                    tokenA = oldState.tokenA,
                    tokenB = oldState.tokenB,
                    amountTokenA = action.newAmount,
                    slippage = oldState.slippage,
                    activeRouteIndex = DEFAULT_ACTIVE_ROUTE_ORDINAL
                )
            }
            is SwapStateAction.TokenAChanged -> {
                stateFlow.value = oldState.copy(tokenA = action.newTokenA)
                swapValidator.validateIsSameTokens(tokenA = action.newTokenA, tokenB = oldState.tokenB)
            }
            is SwapStateAction.TokenBChanged -> {
                stateFlow.value = oldState.copy(tokenB = action.newTokenB)
                swapValidator.validateIsSameTokens(tokenA = oldState.tokenA, tokenB = action.newTokenB)
            }
            SwapStateAction.InitialLoading -> {
                stateFlow.value = SwapState.InitialLoading
            }

            SwapStateAction.SwitchTokens -> {
                val tokenA = oldState.tokenA
                val tokenB = oldState.tokenB
                analytics.logTokensSwitchClicked(tokenA, tokenB)

                stateFlow.value = oldState.copy(
                    tokenA = tokenB,
                    tokenB = tokenA,
                )
            }
            SwapStateAction.RefreshRoutes,
            SwapStateAction.EmptyAmountTokenA,
            is SwapStateAction.ActiveRouteChanged,
            SwapStateAction.CancelSwapLoading -> return
        }
    }
}
