package org.p2p.wallet.history.interactor

import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.repository.remote.HistoryRemoteRepository

class HistoryInteractor(
    private val historyServiceRepository: HistoryRemoteRepository
) {

    suspend fun loadHistory(limit: Int): List<HistoryTransaction> {
        return historyServiceRepository.loadHistory(limit)
    }

    fun findTransactionById(id: String): HistoryTransaction? {
        return historyServiceRepository.findTransactionById(id)
    }
}
