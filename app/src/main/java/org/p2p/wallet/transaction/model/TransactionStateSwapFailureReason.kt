package org.p2p.wallet.transaction.model

sealed class TransactionStateSwapFailureReason {
    data class LowSlippage(val currentSlippageValue: String) : TransactionStateSwapFailureReason()
    data class Unknown(val message: String) : TransactionStateSwapFailureReason()
}
