package org.p2p.wallet.jupiter.statemanager.handler

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.jupiter.statemanager.SwapState
import org.p2p.wallet.jupiter.statemanager.SwapStateAction
import org.p2p.wallet.jupiter.statemanager.SwapStateManager.Companion.DEFAULT_SLIPPAGE
import org.p2p.wallet.jupiter.statemanager.token_selector.SwapInitialTokenSelector

class SwapStateInitialLoadingHandler(
    private val dispatchers: CoroutineDispatchers,
    private val initialTokenSelector: SwapInitialTokenSelector,
) : SwapStateHandler {

    override fun canHandle(state: SwapState): Boolean = state is SwapState.InitialLoading

    override suspend fun handleAction(
        stateFlow: MutableStateFlow<SwapState>,
        state: SwapState,
        action: SwapStateAction
    ) = withContext(dispatchers.io) {
        stateFlow.value = SwapState.InitialLoading
        val (tokenA, tokenB) = initialTokenSelector.getTokenPair()
        stateFlow.value = SwapState.TokenAZero(
            tokenA = tokenA,
            tokenB = tokenB,
            slippage = DEFAULT_SLIPPAGE
        )
    }
}
