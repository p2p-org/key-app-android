package org.p2p.wallet.history.interactor.buffer

import org.p2p.solanaj.kits.transaction.TransactionDetails

interface TransactionListener {
    fun onTransactionsLoaded(items: List<TransactionDetails>)
    fun onBufferFinished(tokenAddress: String)
}
