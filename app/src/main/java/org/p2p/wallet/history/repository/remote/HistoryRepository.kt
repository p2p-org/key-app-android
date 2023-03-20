package org.p2p.wallet.history.repository.remote

import kotlinx.coroutines.withContext
import org.p2p.wallet.common.date.dateMilli
import org.p2p.wallet.history.model.HistoryPagingResult
import org.p2p.wallet.history.model.HistoryPagingState
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.repository.local.TransactionDetailsLocalRepository
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers

class HistoryRepository(
    private val repositories: List<HistoryRemoteRepository>,
    private val dispatchers: CoroutineDispatchers,
    private val localRepository: TransactionDetailsLocalRepository,
) : HistoryRemoteRepository {

    override suspend fun loadHistory(limit: Int, mintAddress: String?): HistoryPagingResult {
        val result = repositories.map { it.loadHistory(limit, mintAddress) }
        return parsePagingResult(result)
    }

    override suspend fun loadNextPage(limit: Int, mintAddress: String?): HistoryPagingResult {
        val result = repositories.map { it.loadNextPage(limit, mintAddress) }
        return parsePagingResult(result)
    }

    override suspend fun findTransactionById(id: String): HistoryTransaction? {
        repositories.forEach { repository ->
            val foundItem = repository.findTransactionById(id)
            if (foundItem != null) {
                return foundItem
            }
        }
        return localRepository.findPendingTransaction(id)
    }

    override fun getPagingState(mintAddress: String?): HistoryPagingState {
        return if (repositories.any { it.getPagingState(mintAddress) == HistoryPagingState.ACTIVE }) {
            HistoryPagingState.ACTIVE
        } else {
            HistoryPagingState.INACTIVE
        }
    }

    private suspend fun parsePagingResult(pagingResult: List<HistoryPagingResult>): HistoryPagingResult =
        withContext(dispatchers.io) {
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
            localRepository.getAllPendingTransactions().forEach {
                if (it.getHistoryTransactionId() in newTransactionIds)
                    localRepository.removePendingTransaction(it.getHistoryTransactionId())
            }
            val pendingTransactions = localRepository.getAllPendingTransactions()

            return@withContext (pendingTransactions + newTransactions).sortedByDescending { transaction ->
                transaction.date.dateMilli()
            }.let(HistoryPagingResult::Success)
        }
}
