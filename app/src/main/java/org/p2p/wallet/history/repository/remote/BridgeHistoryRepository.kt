package org.p2p.wallet.history.repository.remote

import org.p2p.wallet.history.model.HistoryPagingResult
import org.p2p.wallet.history.model.HistoryPagingState
import org.p2p.wallet.history.model.HistoryTransaction

class BridgeHistoryRepository : HistoryRemoteRepository {

    override suspend fun loadHistory(limit: Int, mintAddress: String): HistoryPagingResult {
        TODO("Not yet implemented")
    }

    override suspend fun loadNextPage(limit: Int, mintAddress: String): HistoryPagingResult {
        TODO("Not yet implemented")
    }

    override suspend fun findTransactionById(id: String): HistoryTransaction? {
        TODO("Not yet implemented")
    }

    override fun getPagingState(mintAddress: String?): HistoryPagingState {
        TODO("Not yet implemented")
    }
}
