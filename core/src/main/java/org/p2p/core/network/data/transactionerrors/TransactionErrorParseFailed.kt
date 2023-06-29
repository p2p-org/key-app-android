package org.p2p.core.network.data.transactionerrors

data class TransactionErrorParseFailed(
    override val message: String,
    override val cause: Throwable? = null,
) : Throwable()
