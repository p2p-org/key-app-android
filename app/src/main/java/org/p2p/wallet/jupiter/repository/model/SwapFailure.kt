package org.p2p.wallet.jupiter.repository.model

import java.math.BigInteger

private const val CREATE_SWAP_TRANSACTION_ERROR =
    "Failed to create swap transaction with Jupiter API for route with amount"

sealed class SwapFailure(
    override val message: String,
    override val cause: Throwable? = null
) : Throwable() {
    data class CreateSwapTransactionFailed(
        val amountInLamports: BigInteger,
        override val cause: Throwable
    ) : SwapFailure("$CREATE_SWAP_TRANSACTION_ERROR $amountInLamports")
    data class TooSmallInputAmount(
        override val cause: Throwable
    ) : SwapFailure(cause.message.orEmpty())

    data class ServerUnknownError(
        override val cause: Throwable
    ) : SwapFailure(cause.message.orEmpty())
}
