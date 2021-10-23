package org.p2p.wallet.swap.model.orca

import androidx.annotation.StringRes
import java.math.BigDecimal

sealed class OrcaSwapResult {
    data class Success(
        val transactionId: String,
        val receivedAmount: BigDecimal,
        val usdReceivedAmount: BigDecimal,
        val tokenSymbol: String
    ) : OrcaSwapResult()

    data class Error(@StringRes val messageRes: Int) : OrcaSwapResult()
}