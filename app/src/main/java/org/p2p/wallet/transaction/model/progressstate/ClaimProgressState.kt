package org.p2p.wallet.transaction.model.progressstate

sealed class ClaimProgressState : TransactionState() {

    data class Success(
        val bundleId: String,
        val sourceTokenSymbol: String
    ) : TransactionState.Success()

    data class Error(
        override val message: String,
    ) : TransactionState.Error(message)
}
