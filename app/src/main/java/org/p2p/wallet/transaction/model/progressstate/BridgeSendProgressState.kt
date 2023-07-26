package org.p2p.wallet.transaction.model.progressstate

import org.p2p.wallet.bridge.send.model.BridgeSendTransactionDetails

object BridgeSendProgressState : TransactionState() {

    data class Success(
        val transactionId: String,
        val sendDetails: BridgeSendTransactionDetails
    ) : TransactionState.Success()

    data class Error(
        override val message: String,
    ) : TransactionState.Error(message)
}
