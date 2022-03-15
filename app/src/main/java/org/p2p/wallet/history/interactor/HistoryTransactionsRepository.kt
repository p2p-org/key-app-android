package org.p2p.wallet.history.interactor

import org.p2p.wallet.history.model.HistoryTransaction

interface HistoryTransactionsRepository {
    suspend fun getTransactionsHistory(tokenPublicKey: String, signatures: List<String>): List<HistoryTransaction>
}