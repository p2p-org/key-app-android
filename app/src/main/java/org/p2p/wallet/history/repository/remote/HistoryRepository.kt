package org.p2p.wallet.history.repository.remote

import org.p2p.wallet.common.date.dateMilli
import org.p2p.wallet.history.model.HistoryPagingResult
import org.p2p.wallet.history.model.HistoryPagingState
import org.p2p.wallet.history.model.HistoryTransaction

private const val TAG = "HistoryRepository"

class HistoryRepository(
    private val repositories: List<HistoryRemoteRepository>
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
        return null
    }

    override fun getPagingState(mintAddress: String?): HistoryPagingState {
        return if (repositories.any { it.getPagingState(mintAddress) == HistoryPagingState.ACTIVE }) {
            HistoryPagingState.ACTIVE
        } else {
            HistoryPagingState.INACTIVE
        }
    }

    private fun parsePagingResult(pagingResult: List<HistoryPagingResult>): HistoryPagingResult {
        val newTransactions = mutableListOf<HistoryTransaction>()
        val errorMessageBuilder = StringBuilder()
        pagingResult.forEach { pagingResult ->
            when (pagingResult) {
                is HistoryPagingResult.Error -> {
                    errorMessageBuilder.append(pagingResult.cause)
                    errorMessageBuilder.append("\n")
                }
                is HistoryPagingResult.Success -> {
                    newTransactions.addAll(pagingResult.data)
                }
            }
        }
        val errorMessage = errorMessageBuilder.toString()
        if (errorMessage.isNotEmpty()) {
            return HistoryPagingResult.Error(Throwable(errorMessage))
        }
        return newTransactions.sortedByDescending { transaction ->
            transaction.date.dateMilli()
        }.let(HistoryPagingResult::Success)
    }
}
