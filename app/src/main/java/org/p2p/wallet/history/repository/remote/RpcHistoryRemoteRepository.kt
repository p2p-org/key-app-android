package org.p2p.wallet.history.repository.remote

import org.p2p.solanaj.model.types.RpcMapRequest
import org.p2p.wallet.history.api.RpcHistoryServiceApi
import org.p2p.wallet.history.api.model.RpcHistoryTransactionResponse
import org.p2p.wallet.history.interactor.mapper.RpcHistoryTransactionConverter
import org.p2p.wallet.history.model.rpc.HistoryTransaction
import org.p2p.wallet.history.signature.HistoryServiceSignatureFieldGenerator
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import java.util.Optional

private const val REQUEST_PARAMS_USER_ID = "user_id"
private const val REQUEST_PARAMS_LIMIT = "limit"
private const val REQUEST_PARAMS_OFFSET = "offset"
private const val REQUEST_PARAMS_SIGNATURE = "signature"
private const val REQUEST_PARAMS_NAME = "get_transactions"

class RpcHistoryRemoteRepository(
    private val historyApi: RpcHistoryServiceApi,
    private val tokenKeyProvider: TokenKeyProvider,
    private val historyServiceSignatureFieldGenerator: HistoryServiceSignatureFieldGenerator,
    private val converter: RpcHistoryTransactionConverter
) : HistoryRemoteRepository {

    override suspend fun loadHistory(limit: Int, offset: Int): List<HistoryTransaction> {

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
        val response = historyApi.getTransactionHistory(rpcRequest).result
        return converter.mapTransactionDetailsToHistoryTransactions(response)
    }
}
