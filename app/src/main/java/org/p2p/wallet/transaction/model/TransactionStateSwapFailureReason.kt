package org.p2p.wallet.transaction.model

sealed class TransactionStateSwapFailureReason {
    data class LowSlippage(val currentSlippageValue: Double) : TransactionStateSwapFailureReason()
    data class Unknown(val message: String) : TransactionStateSwapFailureReason()
}
