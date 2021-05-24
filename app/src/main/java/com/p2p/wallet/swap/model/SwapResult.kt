package com.p2p.wallet.swap.model

import androidx.annotation.StringRes
import java.math.BigDecimal

sealed class SwapResult {
    data class Success(
        val transactionId: String,
        val receivedAmount: BigDecimal,
        val usdReceivedAmount: BigDecimal,
        val tokenSymbol: String
    ) : SwapResult()

    data class Error(@StringRes val messageRes: Int) : SwapResult()
}