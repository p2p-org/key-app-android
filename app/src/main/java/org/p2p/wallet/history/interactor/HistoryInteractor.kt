package org.p2p.wallet.history.interactor

import kotlinx.coroutines.withContext
import org.p2p.wallet.history.model.HistoryPagingResult
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.repository.local.PendingTransactionsLocalRepository
import org.p2p.wallet.history.repository.remote.HistoryRemoteRepository
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.crypto.Base58String

class HistoryInteractor(
    private val historyServiceRepository: HistoryRemoteRepository,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val pendingTransactionsLocalRepository: PendingTransactionsLocalRepository
) {
    // move withContext to repo according to styleguide
    suspend fun loadHistory(limit: Int, mintAddress: String): HistoryPagingResult =
        withContext(coroutineDispatchers.io) {
            historyServiceRepository.loadHistory(limit, mintAddress)
        }

    // move withContext to repo according to styleguide
    suspend fun loadNextPage(limit: Int, mintAddress: String): HistoryPagingResult =
        withContext(coroutineDispatchers.io) {
            historyServiceRepository.loadNextPage(limit, mintAddress)
        }

    // move withContext to repo according to styleguide
    suspend fun findTransactionById(id: String): HistoryTransaction? {
        return historyServiceRepository.findTransactionById(id)
    }

    suspend fun addPendingTransaction(txSignature: String, mintAddress: Base58String, transaction: HistoryTransaction) {
        pendingTransactionsLocalRepository.savePendingTransaction(
            mintAddress = mintAddress,
            txSignature = txSignature,
            transaction = transaction
        )
    }
}
