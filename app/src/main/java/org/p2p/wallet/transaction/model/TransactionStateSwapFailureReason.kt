package org.p2p.wallet.transaction.model

import org.p2p.wallet.swap.model.Slippage

sealed class TransactionStateSwapFailureReason {
    data class LowSlippage(val currentSlippageValue: Slippage) : TransactionStateSwapFailureReason()
    data class Unknown(val message: String) : TransactionStateSwapFailureReason()
}
