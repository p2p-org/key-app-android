package org.p2p.wallet.history.interactor

import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.repository.remote.HistoryRemoteRepository

class HistoryServiceInteractor(
    private val historyServiceRepository: HistoryRemoteRepository
) {

    suspend fun loadHistory(limit: Int, offset: Int): List<HistoryTransaction> {
        return historyServiceRepository.loadHistory(limit, offset)
    }

    fun findTransactionBySignature(signature: String): HistoryTransaction? {
        return historyServiceRepository.findTransactionById(signature)
    }
}
