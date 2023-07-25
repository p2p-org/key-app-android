package org.p2p.wallet.transaction.model.progressstate

import org.p2p.wallet.transaction.model.TransactionStateSwapFailureReason

sealed class JupiterSwapProgressState : TransactionState() {

    object Success : TransactionState.Success()

    data class Error(
        val failure: TransactionStateSwapFailureReason
    ) : TransactionState.Error(failure.toString())
}
