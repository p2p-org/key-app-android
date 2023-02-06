package org.p2p.wallet.history.interactor

import org.p2p.wallet.history.api.model.RpcHistoryResponse
import org.p2p.wallet.history.repository.remote.HistoryRemoteRepository

class HistoryServiceInteractor(
    private val historyRemoteRepository: HistoryRemoteRepository
) {
    suspend fun loadHistory(
        limit: Int,
        offset: Int,
    ): List<RpcHistoryResponse> {
        return historyRemoteRepository.loadHistory(limit, offset)
    }
}
