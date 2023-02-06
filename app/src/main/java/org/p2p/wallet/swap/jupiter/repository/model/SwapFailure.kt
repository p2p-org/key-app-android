package org.p2p.wallet.swap.jupiter.repository.model

private const val CREATE_SWAP_TRANSACTION_ERROR =
    "Failed to create swap transaction with Jupiter API for route with amount"

sealed class SwapFailure(
    override val message: String,
    override val cause: Throwable? = null
) : Throwable() {
    data class CreateSwapTransactionFailed(
        val route: JupiterSwapRoute,
        override val cause: Throwable
    ) : SwapFailure("$CREATE_SWAP_TRANSACTION_ERROR ${route.amountInLamports}")
}
