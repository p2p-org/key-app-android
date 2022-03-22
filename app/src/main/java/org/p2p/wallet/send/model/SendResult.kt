package org.p2p.wallet.send.model

import androidx.annotation.StringRes

sealed class SendResult {
    data class Success(val transactionId: String) : SendResult()

    object WrongWallet : SendResult()

    data class Error(@StringRes val messageRes: Int) : SendResult()
}
