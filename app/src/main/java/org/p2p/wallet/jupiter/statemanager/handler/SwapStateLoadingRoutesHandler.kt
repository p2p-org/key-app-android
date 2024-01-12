package org.p2p.wallet.jupiter.statemanager.handler

import java.math.BigDecimal
import kotlinx.coroutines.flow.MutableStateFlow
import org.p2p.wallet.infrastructure.swap.JupiterSwapStorageContract
import org.p2p.wallet.jupiter.analytics.JupiterSwapMainScreenAnalytics
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.jupiter.statemanager.SwapState
import org.p2p.wallet.jupiter.statemanager.SwapStateAction
import org.p2p.wallet.jupiter.statemanager.SwapStateRoutesRefresher
import org.p2p.wallet.swap.model.Slippage

class SwapStateLoadingRoutesHandler(
    private val routesRefresher: SwapStateRoutesRefresher,
    private val selectedTokensStorage: JupiterSwapStorageContract,
    private val analytics: JupiterSwapMainScreenAnalytics
) : SwapStateHandler {

    override fun canHandle(state: SwapState): Boolean = state is SwapState.LoadingRoutes

    override suspend fun handleAction(
        stateFlow: MutableStateFlow<SwapState>,
        state: SwapState,
        action: SwapStateAction
    ) {
        val oldState = state as SwapState.LoadingRoutes

        var tokenA: SwapTokenModel = oldState.tokenA
        var tokenB: SwapTokenModel = oldState.tokenB
        var amountTokenA: BigDecimal = oldState.amountTokenA
        var slippage: Slippage = oldState.slippage

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

            SwapStateAction.EmptyAmountTokenA -> {
                stateFlow.value = SwapState.TokenAZero(tokenA, tokenB, slippage)
                return
            }
            SwapStateAction.InitialLoading -> {
                stateFlow.value = SwapState.InitialLoading
                return
            }
            SwapStateAction.CancelSwapLoading -> return
            SwapStateAction.RefreshRoutes -> Unit
        }

        selectedTokensStorage.savedTokenAMint = tokenA.mintAddress
        selectedTokensStorage.savedTokenBMint = tokenB.mintAddress

        routesRefresher.refreshRoutes(
            state = stateFlow,
            tokenA = tokenA,
            tokenB = tokenB,
            amountTokenA = amountTokenA,
            slippage = slippage,
        )
    }
}
