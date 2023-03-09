package org.p2p.wallet.swap.jupiter.statemanager.handler

import java.math.BigDecimal
import kotlinx.coroutines.flow.MutableStateFlow
import org.p2p.wallet.swap.jupiter.analytics.JupiterSwapMainScreenAnalytics
import org.p2p.wallet.swap.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.swap.jupiter.statemanager.SwapState
import org.p2p.wallet.swap.jupiter.statemanager.SwapStateAction
import org.p2p.wallet.swap.jupiter.statemanager.SwapStateManager.Companion.DEFAULT_ACTIVE_ROUTE_ORDINAL
import org.p2p.wallet.swap.jupiter.statemanager.SwapStateRoutesRefresher
import org.p2p.wallet.swap.model.Slippage

class SwapStateSwapLoadedHandler(
    private val routesRefresher: SwapStateRoutesRefresher,
    private val analytics: JupiterSwapMainScreenAnalytics,
) : SwapStateHandler {

    override fun canHandle(state: SwapState): Boolean = state is SwapState.SwapLoaded

    override suspend fun handleAction(
        stateFlow: MutableStateFlow<SwapState>,
        state: SwapState,
        action: SwapStateAction,
    ) {
        val oldState = state as SwapState.SwapLoaded

        var tokenA: SwapTokenModel = oldState.tokenA
        var tokenB: SwapTokenModel = oldState.tokenB
        var amountTokenA: BigDecimal = oldState.amountTokenA
        var slippage: Slippage = oldState.slippage
        var activeRouteOrdinal = oldState.activeRoute

        when (action) {
            is SwapStateAction.SlippageChanged -> slippage = action.newSlippageValue
            SwapStateAction.SwitchTokens -> {
                tokenA = oldState.tokenB
                tokenB = oldState.tokenA
                analytics.logTokensSwitchClicked(tokenA, tokenB)
            }
            is SwapStateAction.TokenAAmountChanged -> amountTokenA = action.newAmount
            is SwapStateAction.TokenAChanged -> tokenA = action.newTokenA
            is SwapStateAction.TokenBChanged -> tokenB = action.newTokenB
            is SwapStateAction.ActiveRouteChanged -> activeRouteOrdinal = action.ordinalRouteNumber
            SwapStateAction.RefreshRoutes -> activeRouteOrdinal = DEFAULT_ACTIVE_ROUTE_ORDINAL

            SwapStateAction.InitialLoading, SwapStateAction.EmptyAmountTokenA -> {
                stateFlow.value = SwapState.TokenAZero(tokenA, tokenB, slippage)
                return
            }
            SwapStateAction.CancelSwapLoading -> return
        }

        routesRefresher.refreshRoutes(
            state = stateFlow,
            tokenA = tokenA,
            tokenB = tokenB,
            amountTokenA = amountTokenA,
            slippage = slippage,
            activeRouteIndex = activeRouteOrdinal
        )
    }
}
