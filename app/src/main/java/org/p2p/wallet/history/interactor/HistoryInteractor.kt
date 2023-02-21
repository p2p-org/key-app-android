package org.p2p.wallet.history.interactor

import org.p2p.wallet.history.model.HistoryPagingResult
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.repository.remote.HistoryRemoteRepository

class HistoryInteractor(
    private val historyServiceRepository: HistoryRemoteRepository
) {

    suspend fun loadHistory(limit: Int, mintAddress: String?): HistoryPagingResult {
        return historyServiceRepository.loadHistory(limit, mintAddress)
    }

    suspend fun loadNextPage(limit: Int, mintAddress: String?): HistoryPagingResult {
        return historyServiceRepository.loadNextPage(limit, mintAddress)
    }

    suspend fun findTransactionById(id: String): HistoryTransaction? {
        val foundTransaction = historyServiceRepository.findTransactionById(id)
        return foundTransaction
    }
}
