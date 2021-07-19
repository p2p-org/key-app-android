package com.p2p.wallet.main.model

import androidx.annotation.StringRes

sealed class TransactionResult {
    data class Success(val signature: String) : TransactionResult()

    object WrongWallet : TransactionResult()

    data class Error(@StringRes val messageRes: Int) : TransactionResult()
}