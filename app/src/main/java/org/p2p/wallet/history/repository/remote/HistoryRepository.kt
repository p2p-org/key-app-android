package org.p2p.wallet.history.repository.remote

import kotlinx.coroutines.withContext
import org.p2p.wallet.common.date.dateMilli
import org.p2p.wallet.history.model.HistoryPagingResult
import org.p2p.wallet.history.model.HistoryPagingState
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.repository.local.PendingTransactionsLocalRepository
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.toBase58Instance

class HistoryRepository(
    private val repositories: List<HistoryRemoteRepository>,
    private val dispatchers: CoroutineDispatchers,
    private val pendingTransactionsLocalRepository: PendingTransactionsLocalRepository
) : HistoryRemoteRepository {

    override suspend fun loadHistory(limit: Int, mintAddress: String): HistoryPagingResult {
        val result = repositories.map { it.loadHistory(limit, mintAddress) }
        return parsePagingResult(pagingResult = result, mintAddress = mintAddress)
    }

    override suspend fun loadNextPage(limit: Int, mintAddress: String): HistoryPagingResult {
        val result = repositories.map { it.loadNextPage(limit, mintAddress) }
        return parsePagingResult(pagingResult = result, mintAddress = mintAddress)
    }

    override suspend fun findTransactionById(id: String): HistoryTransaction? {
        repositories.forEach { repository ->
            val foundItem = repository.findTransactionById(id)
            if (foundItem != null) {
                return foundItem
            }
        }
        return pendingTransactionsLocalRepository.findPendingTransaction(id)
    }

    override fun getPagingState(mintAddress: String?): HistoryPagingState {
        return if (repositories.any { it.getPagingState(mintAddress) == HistoryPagingState.ACTIVE }) {
            HistoryPagingState.ACTIVE
        } else {
            HistoryPagingState.INACTIVE
        }
    }

    private suspend fun parsePagingResult(
        pagingResult: List<HistoryPagingResult>,
        mintAddress: String,
    ): HistoryPagingResult = withContext(dispatchers.io) {
        val newTransactions = mutableListOf<HistoryTransaction>()
        val errorMessageBuilder = StringBuilder()
        pagingResult.forEach { result ->
            when (result) {
                is HistoryPagingResult.Error -> {
                    errorMessageBuilder.append(result.cause)
                    errorMessageBuilder.append("\n")
                }
                is HistoryPagingResult.Success -> {
                    newTransactions.addAll(result.data)
                }
            }
        }
        val errorMessage = errorMessageBuilder.toString()
        if (errorMessage.isNotEmpty()) {
            return@withContext HistoryPagingResult.Error(Throwable(errorMessage))
        }
        val newTransactionIds = newTransactions.map { it.getHistoryTransactionId() }
        val pendingItems = getPendingTransactions(newTransactionIds, mintAddress.toBase58Instance())

        (pendingItems + newTransactions)
            .sortedByDescending { transaction -> transaction.date.dateMilli() }
            .let(HistoryPagingResult::Success)
    }

    private suspend fun getPendingTransactions(
        newTransactionIds: List<String>,
        mintAddress: Base58String,
    ): List<HistoryTransaction> {
        pendingTransactionsLocalRepository.getAllPendingTransactions(mintAddress)
            .forEach { removePendingTransactionIfExists(it, newTransactionIds) }
        return pendingTransactionsLocalRepository.getAllPendingTransactions(mintAddress)
    }

    private suspend fun removePendingTransactionIfExists(
        localItem: HistoryTransaction,
        newTransactionIds: List<String>
    ) {
        val txSignature = localItem.getHistoryTransactionId()
        if (txSignature in newTransactionIds) {
            pendingTransactionsLocalRepository.removePendingTransaction(txSignature)
        }
    }
}
