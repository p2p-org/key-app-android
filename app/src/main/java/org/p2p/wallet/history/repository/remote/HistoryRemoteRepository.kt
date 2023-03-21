package org.p2p.wallet.history.repository.remote

import org.p2p.wallet.history.model.HistoryPagingResult
import org.p2p.wallet.history.model.HistoryPagingState
import org.p2p.wallet.history.model.HistoryTransaction

interface HistoryRemoteRepository {

    suspend fun loadHistory(limit: Int, mintAddress: String): HistoryPagingResult
    suspend fun loadNextPage(limit: Int, mintAddress: String): HistoryPagingResult

    suspend fun findTransactionById(id: String): HistoryTransaction?

    fun getPagingState(mintAddress: String?): HistoryPagingState
}
