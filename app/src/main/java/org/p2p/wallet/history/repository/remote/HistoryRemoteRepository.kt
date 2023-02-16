package org.p2p.wallet.history.repository.remote

import org.p2p.wallet.history.model.HistoryTransaction

interface HistoryRemoteRepository {
    suspend fun loadHistory(limit: Int): List<HistoryTransaction>
    fun findTransactionById(id: String): HistoryTransaction?
}
