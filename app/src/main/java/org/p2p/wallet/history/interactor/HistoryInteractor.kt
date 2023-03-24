package org.p2p.wallet.history.interactor

import kotlinx.coroutines.withContext
import org.p2p.wallet.history.model.HistoryPagingResult
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.repository.local.TransactionDetailsLocalRepository
import org.p2p.wallet.history.repository.remote.HistoryRemoteRepository
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers

class HistoryInteractor(
    private val historyServiceRepository: HistoryRemoteRepository,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val localRepository: TransactionDetailsLocalRepository,
) {
    suspend fun loadHistory(limit: Int, mintAddress: String): HistoryPagingResult =
        withContext(coroutineDispatchers.io) {
            historyServiceRepository.loadHistory(limit, mintAddress)
        }

    suspend fun loadNextPage(limit: Int, mintAddress: String): HistoryPagingResult =
        withContext(coroutineDispatchers.io) {
            historyServiceRepository.loadNextPage(limit, mintAddress)
        }

    suspend fun findTransactionById(id: String): HistoryTransaction? {
        return historyServiceRepository.findTransactionById(id)
    }

    suspend fun addPendingTransaction(txSignature: String, mintAddress: String, transaction: HistoryTransaction) {
        localRepository.savePendingTransaction(
            mintAddress = mintAddress,
            txSignature = txSignature,
            transaction = transaction
        )
    }
}
