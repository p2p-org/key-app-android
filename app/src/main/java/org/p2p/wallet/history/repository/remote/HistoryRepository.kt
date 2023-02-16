package org.p2p.wallet.history.repository.remote

import org.p2p.wallet.history.model.HistoryPagingResult
import org.p2p.wallet.history.model.HistoryPagingState
import org.p2p.wallet.history.model.HistoryTransaction
import timber.log.Timber

private const val TAG = "HistoryRepository"

class HistoryRepository(
    private val repositories: List<HistoryRemoteRepository>
) : HistoryRemoteRepository {

    override suspend fun loadHistory(limit: Int, mintAddress: String?): HistoryPagingResult {
        val result = repositories.filter { it.getPagingState() == HistoryPagingState.INITIAL }
            .map { it.loadHistory(limit, mintAddress) }
        return parsePagingResult(result)
    }

    override suspend fun loadNextPage(limit: Int, mintAddress: String?): HistoryPagingResult {
        val result = repositories.filter { it.getPagingState() == HistoryPagingState.INITIAL }
            .map { it.loadNextPage(limit) }
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

    override fun getPagingState(): HistoryPagingState {
        return if (repositories.any { it.getPagingState() == HistoryPagingState.INITIAL }) {
            HistoryPagingState.INITIAL
        } else {
            HistoryPagingState.IDLE
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
        Timber.tag(TAG).d("Error stack trace: $errorMessage")
        Timber.tag(TAG).d("New transaction fetched, size = ${newTransactions.size}")
        if (errorMessage.isNotEmpty()) {
            return HistoryPagingResult.Error(Throwable(errorMessage))
        }

        return HistoryPagingResult.Success(newTransactions)
    }
}
