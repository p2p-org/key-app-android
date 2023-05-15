package org.p2p.wallet.infrastructure.network.data.transactionerrors

data class TransactionErrorParseFailed(
    override val message: String,
    override val cause: Throwable? = null,
) : Throwable()
