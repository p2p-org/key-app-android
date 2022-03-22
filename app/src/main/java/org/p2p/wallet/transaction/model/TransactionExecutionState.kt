package org.p2p.wallet.transaction.model

/**
 * @param transactionId is id for actualizing active state and get info about transactions which are being executed
 * [transactionId] is only used for local purposes, there is no such param when making API request
 * */
sealed class TransactionExecutionState(
    open val transactionId: String?
) {
    object Idle : TransactionExecutionState(null)

    data class Executing(
        override val transactionId: String
    ) : TransactionExecutionState(transactionId)

    data class Finished(
        override val transactionId: String,
        val signature: String
    ) : TransactionExecutionState(transactionId)

    data class Failed(
        override val transactionId: String,
        val throwable: Throwable
    ) : TransactionExecutionState(transactionId)
}
