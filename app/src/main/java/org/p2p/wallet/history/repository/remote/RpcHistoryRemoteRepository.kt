package org.p2p.wallet.history.repository.remote

import org.p2p.solanaj.model.types.RpcMapRequest
import org.p2p.wallet.history.api.HistoryServiceApi
import org.p2p.wallet.history.api.model.RpcHistoryResponse
import org.p2p.wallet.history.signature.HistoryServiceSignatureFieldGenerator
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import java.util.Optional

private const val REQUEST_PARAMS_USER_ID = "user_id"
private const val REQUEST_PARAMS_LIMIT = "limit"
private const val REQUEST_PARAMS_OFFSET = "offset"
private const val REQUEST_PARAMS_SIGNATURE = "signature"
private const val REQUEST_PARAMS_NAME = "get_transactions"

class RpcHistoryRemoteRepository(
    private val historyApi: HistoryServiceApi,
    private val tokenKeyProvider: TokenKeyProvider,
    private val historyServiceSignatureFieldGenerator: HistoryServiceSignatureFieldGenerator
) : HistoryRemoteRepository {

    override suspend fun loadHistory(limit: Int, offset: Int): List<RpcHistoryResponse> {

        val signature = historyServiceSignatureFieldGenerator.generateSignature(
            pubKey = tokenKeyProvider.publicKey,
            privateKey = tokenKeyProvider.keyPair,
            offset = offset.toLong(),
            limit = limit.toLong(),
            mint = Optional.empty()
        )
        val requestParams = mapOf(
            REQUEST_PARAMS_USER_ID to tokenKeyProvider.publicKey,
            REQUEST_PARAMS_LIMIT to limit,
            REQUEST_PARAMS_OFFSET to offset,
            REQUEST_PARAMS_SIGNATURE to signature
        )
        val rpcRequest = RpcMapRequest(
            method = REQUEST_PARAMS_NAME,
            params = requestParams
        )
        return historyApi.getTransactionHistory(rpcRequest).result
    }
}
