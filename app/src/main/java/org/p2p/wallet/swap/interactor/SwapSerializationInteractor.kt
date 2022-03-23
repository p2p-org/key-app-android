package org.p2p.wallet.swap.interactor

import org.p2p.wallet.transaction.TransactionManager
import org.p2p.wallet.transaction.model.AppTransaction

class SwapSerializationInteractor(
    private val transactionManager: TransactionManager
) {

    fun sendTransaction(appTransaction: AppTransaction) {
        transactionManager.addInQueue(appTransaction)
    }
}
