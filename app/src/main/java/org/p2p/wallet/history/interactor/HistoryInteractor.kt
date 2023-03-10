package org.p2p.wallet.history.interactor

import kotlinx.coroutines.withContext
import org.p2p.wallet.history.model.HistoryPagingResult
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.repository.remote.HistoryRemoteRepository
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers

class HistoryInteractor(
    private val historyServiceRepository: HistoryRemoteRepository,
    private val coroutineDispatchers: CoroutineDispatchers
) {

    suspend fun loadHistory(limit: Int, mintAddress: String?): HistoryPagingResult =
        withContext(coroutineDispatchers.io) {
            return@withContext historyServiceRepository.loadHistory(limit, mintAddress)
        }

    suspend fun loadNextPage(limit: Int, mintAddress: String?): HistoryPagingResult =
        withContext(coroutineDispatchers.io) {
            return@withContext historyServiceRepository.loadNextPage(limit, mintAddress)
        }

    suspend fun findTransactionById(id: String): HistoryTransaction? {
        return historyServiceRepository.findTransactionById(id)
    }
}
