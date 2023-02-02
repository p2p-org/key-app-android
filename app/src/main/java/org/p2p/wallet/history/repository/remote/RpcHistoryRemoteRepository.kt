package org.p2p.wallet.history.repository.remote

import org.p2p.solanaj.model.types.RpcMapRequest
import org.p2p.wallet.history.api.HistoryServiceApi
import org.p2p.wallet.history.api.model.RpcHistoryResponse

private const val REQUEST_PARAMS_USER_ID = "user_id"
private const val REQUEST_PARAMS_LIMIT = "limit"
private const val REQUEST_PARAMS_OFFSET = "offset"
private const val REQUEST_PARAMS_NAME = "get_transactions"

class RpcHistoryRemoteRepository(
    private val historyApi: HistoryServiceApi
) {

    suspend fun getHistory(userId: String, limit: Int, offset: Int): List<RpcHistoryResponse> {
        val requestParams = mapOf(
            REQUEST_PARAMS_USER_ID to userId,
            REQUEST_PARAMS_LIMIT to limit,
            REQUEST_PARAMS_OFFSET to offset
        )
        val rpcRequest = RpcMapRequest(
            method = REQUEST_PARAMS_NAME,
            params = requestParams
        )
        return historyApi.getTransactionHistory(rpcRequest).result
    }
}
