package org.p2p.wallet.history.interactor

import org.p2p.wallet.history.api.model.RpcHistoryResponse
import org.p2p.wallet.history.repository.remote.RpcHistoryRemoteRepository
import org.p2p.wallet.utils.toBase58Instance

class HistoryServiceInteractor(
    private val historyServiceRepository: RpcHistoryRemoteRepository
) {

    suspend fun loadHistory(publicKey: String, limit: Int, offset: Int): List<RpcHistoryResponse> {
        return historyServiceRepository.getHistory(publicKey.toBase58Instance().base58Value, limit, offset)
    }
}
