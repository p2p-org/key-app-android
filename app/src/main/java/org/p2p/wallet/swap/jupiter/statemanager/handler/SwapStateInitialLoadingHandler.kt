package org.p2p.wallet.swap.jupiter.statemanager.handler

import org.p2p.wallet.swap.jupiter.statemanager.SwapState
import org.p2p.wallet.swap.jupiter.statemanager.SwapStateAction
import org.p2p.wallet.swap.jupiter.statemanager.SwapStateManager.Companion.DEFAULT_SLIPPAGE
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.swap.jupiter.statemanager.token_selector.InitialTokenSelector

class SwapStateInitialLoadingHandler(
    private val dispatchers: CoroutineDispatchers,
    private val initialTokenSelector: InitialTokenSelector,
) : SwapStateHandler {

    override fun canHandle(state: SwapState): Boolean = state is SwapState.InitialLoading

    override suspend fun handleAction(
        stateFlow: MutableStateFlow<SwapState>,
        state: SwapState,
        action: SwapStateAction
    ) = withContext(dispatchers.io) {
        val oldState = state as SwapState.InitialLoading

        when (action) {
            is SwapStateAction.InitialLoading -> {
                stateFlow.value = SwapState.InitialLoading
                val (tokenA, tokenB) = initialTokenSelector.getTokenPair()
                stateFlow.value = SwapState.TokenAZero(
                    tokenA = tokenA,
                    tokenB = tokenB,
                    slippage = DEFAULT_SLIPPAGE
                )
            }
            else -> Unit
        }
    }
}
