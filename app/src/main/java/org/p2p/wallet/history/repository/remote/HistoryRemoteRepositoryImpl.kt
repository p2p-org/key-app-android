package org.p2p.wallet.history.repository.remote

import org.p2p.wallet.history.api.model.RpcHistoryResponse

class HistoryRemoteRepositoryImpl(
    private val repositories: List<HistoryRemoteRepository>
) : HistoryRemoteRepository {

    override suspend fun loadHistory(limit: Int, offset: Int): List<RpcHistoryResponse> {
        return repositories.flatMap { it.loadHistory(limit, offset) }
    }
}
