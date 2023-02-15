package org.p2p.wallet.swap.jupiter.statemanager

import java.math.BigDecimal
import org.p2p.core.token.Token
import org.p2p.solanaj.utils.crypto.Base64String
import org.p2p.wallet.swap.jupiter.repository.model.JupiterSwapRoute

sealed interface SwapState {
    object InitialLoading : SwapState
    data class TokenAZero(
        val tokenA: Token.Active,
        val tokenB: Token,
        val slippage: Double
    ) : SwapState

    data class LoadingRoutes(
        val tokenA: Token.Active,
        val tokenB: Token,
        val amountTokenA: BigDecimal,
        val slippage: Double
    ) : SwapState

    data class LoadingTransaction(
        val tokenA: Token.Active,
        val tokenB: Token,
        val amountTokenA: BigDecimal,
        val routes: List<JupiterSwapRoute>,
        val activeRoute: Int,
        val amountTokenB: BigDecimal,
        val slippage: Double
    ) : SwapState

    data class SwapLoaded(
        val tokenA: Token.Active,
        val tokenB: Token,
        val amountTokenA: BigDecimal,
        val amountTokenB: BigDecimal,
        val routes: List<JupiterSwapRoute>,
        val activeRoute: Int,
        val swapBlockchainTransaction: Base64String,
        val slippage: Double
    ) : SwapState
}
