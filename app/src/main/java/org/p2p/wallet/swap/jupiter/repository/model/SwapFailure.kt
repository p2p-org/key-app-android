package org.p2p.wallet.swap.jupiter.repository.model

sealed class SwapFailure(
    override val message: String,
    override val cause: Throwable? = null
) : Throwable() {
    data class CreateSwapTransactionFailed(
        val route: SwapRoute,
        override val cause: Throwable
    ) : SwapFailure("Failed to create swap transaction with Jupiter API for route with amount: ${route.amount}")
}
