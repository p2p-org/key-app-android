package org.p2p.wallet.history.repository.remote

import org.p2p.wallet.history.api.model.RpcHistoryResponse

interface HistoryRemoteRepository {
    suspend fun loadHistory(limit: Int, offset: Int): List<RpcHistoryResponse>
}
