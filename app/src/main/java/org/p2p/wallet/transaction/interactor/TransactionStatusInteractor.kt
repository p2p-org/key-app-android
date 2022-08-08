package org.p2p.wallet.transaction.interactor

import org.p2p.wallet.infrastructure.transactionmanager.TransactionManager

class TransactionStatusInteractor(
    private val transactionManager: TransactionManager
) {

    var onSignatureReceived: ((String) -> Unit)? = null

    fun onSignatureReceived(signature: String) {
        onSignatureReceived?.invoke(signature)
    }
}
