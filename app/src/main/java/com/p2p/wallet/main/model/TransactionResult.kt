package com.p2p.wallet.main.model

import androidx.annotation.StringRes
import java.math.BigDecimal

sealed class TransactionResult {
    data class Success(
        val transactionId: String,
        val amount: BigDecimal,
        val usdAmount: BigDecimal,
        val tokenSymbol: String
    ) : TransactionResult()

    object WrongWallet : TransactionResult()

    data class Error(@StringRes val messageRes: Int) : TransactionResult()
}