package org.p2p.wallet.swap.jupiter.statemanager.handler

import org.p2p.wallet.swap.jupiter.domain.model.SwapTokenModel
import org.p2p.wallet.swap.jupiter.repository.tokens.JupiterSwapTokensRepository
import org.p2p.wallet.swap.jupiter.statemanager.SwapState
import org.p2p.wallet.swap.jupiter.statemanager.SwapStateAction
import org.p2p.wallet.swap.jupiter.statemanager.SwapStateManager.Companion.DEFAULT_SLIPPAGE
import kotlinx.coroutines.flow.MutableStateFlow

class SwapStateInitialLoadingHandler(
    private val jupiterTokensRepository: JupiterSwapTokensRepository,
) : SwapStateHandler {

    override fun canHandle(state: SwapState): Boolean = state is SwapState.InitialLoading

    override suspend fun handleAction(
        stateFlow: MutableStateFlow<SwapState>,
        state: SwapState,
        action: SwapStateAction
    ) {
        val oldState = state as SwapState.InitialLoading

        when (action) {
            is SwapStateAction.InitialLoading -> {
                stateFlow.value = SwapState.InitialLoading

                val jupiterTokens = jupiterTokensRepository.getTokens()
                val first = jupiterTokens.find { it.tokenName == "SOL" } ?: return
                val second = jupiterTokens.find { it.tokenName == "USDC" } ?: return
                stateFlow.value = SwapState.TokenAZero(
                    tokenA = SwapTokenModel.JupiterToken(first, first.logoUri),
                    tokenB = SwapTokenModel.JupiterToken(second, second.logoUri),
                    slippage = DEFAULT_SLIPPAGE
                )
            }
            else -> Unit
        }
    }
}
