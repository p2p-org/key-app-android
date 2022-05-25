package org.p2p.wallet.history.interactor.buffer

import org.p2p.wallet.history.model.HistoryTransaction

interface TransactionListener {
    fun onTransactionsLoaded(items: List<HistoryTransaction>)
    fun onBufferFinished(tokenAddress: String)
}
