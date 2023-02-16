package org.p2p.wallet.history.interactor

import org.p2p.wallet.history.model.HistoryPagingResult
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.repository.remote.HistoryRemoteRepository
import timber.log.Timber

class HistoryInteractor(
    private val historyServiceRepository: HistoryRemoteRepository
) {

    suspend fun loadHistory(limit: Int): HistoryPagingResult {
        return historyServiceRepository.loadHistory(limit)
    }

    suspend fun loadNextPage(limit: Int): HistoryPagingResult {
        return historyServiceRepository.loadNextPage(limit)
    }

    suspend fun findTransactionById(id: String): HistoryTransaction? {
        val foundTransaction = historyServiceRepository.findTransactionById(id)
        Timber.tag("______found").d(foundTransaction.toString())
        return foundTransaction
    }
}
