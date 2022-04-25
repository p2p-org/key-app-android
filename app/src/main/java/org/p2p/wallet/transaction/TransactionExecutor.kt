package org.p2p.wallet.transaction

import kotlinx.coroutines.flow.MutableStateFlow
import org.p2p.wallet.transaction.model.TransactionExecutionState

interface TransactionExecutor {
    fun getStateFlow(): MutableStateFlow<TransactionExecutionState>
    fun getTransactionId(): String
    suspend fun execute()
}
