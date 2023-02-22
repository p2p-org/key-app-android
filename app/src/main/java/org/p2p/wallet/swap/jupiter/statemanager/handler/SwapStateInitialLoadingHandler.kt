package org.p2p.wallet.swap.jupiter.statemanager.handler

import org.p2p.wallet.swap.jupiter.repository.tokens.JupiterSwapTokensRepository
import org.p2p.wallet.swap.jupiter.statemanager.SwapState
import org.p2p.wallet.swap.jupiter.statemanager.SwapStateAction
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

            }
            else -> Unit
        }
    }
}
