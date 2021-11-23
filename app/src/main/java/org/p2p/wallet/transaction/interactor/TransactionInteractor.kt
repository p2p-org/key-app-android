package org.p2p.wallet.transaction.interactor

import kotlinx.coroutines.flow.Flow
import org.p2p.wallet.transaction.TransactionSendManager
import org.p2p.wallet.transaction.model.TransactionExecutionState

class TransactionInteractor(
    private val transactionManager: TransactionSendManager
) {

    var onSignatureReceived: ((String) -> Unit)? = null

    fun onSignatureReceived(signature: String) {
        onSignatureReceived?.invoke(signature)
    }

    fun getTransactionStateFlow(transactionId: String): Flow<TransactionExecutionState>? =
        transactionManager.getStateFlow(transactionId)
}