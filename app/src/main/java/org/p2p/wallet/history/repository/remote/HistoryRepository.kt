package org.p2p.wallet.history.repository.remote

import kotlinx.coroutines.withContext
import org.p2p.core.crypto.Base58String
import org.p2p.core.crypto.toBase58Instance
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.wallet.common.date.dateMilli
import org.p2p.wallet.history.model.HistoryPagingResult
import org.p2p.wallet.history.model.HistoryPagingState
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.repository.local.PendingTransactionsLocalRepository

class HistoryRepository(
    private val repositories: List<HistoryRemoteRepository>,
    private val dispatchers: CoroutineDispatchers,
    private val pendingTransactionsLocalRepository: PendingTransactionsLocalRepository,
    private val pendingTransactionsCleaner: HistoryPendingTransactionsCleaner
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
        val pendingItems = getValidatedPendingTransactions(newTransactions, mintAddress.toBase58Instance())

        (pendingItems + newTransactions)
            .sortedByDescending { transaction -> transaction.date.dateMilli() }
            .let(HistoryPagingResult::Success)
    }

    private suspend fun getValidatedPendingTransactions(
        newTransactions: List<HistoryTransaction>,
        tokenMintAddress: Base58String,
    ): List<HistoryTransaction> {
        val currentPendingTransactions = pendingTransactionsLocalRepository.getAllPendingTransactions(tokenMintAddress)
        pendingTransactionsCleaner.removeCompletedPendingTransactions(
            pendingTransactions = currentPendingTransactions,
            newTransactions = newTransactions
        )

        val finalPendingTransactions = pendingTransactionsLocalRepository.getAllPendingTransactions(tokenMintAddress)
        return finalPendingTransactions
    }
}
