package org.p2p.wallet.history.repository.remote

import org.p2p.solanaj.model.types.RpcMapRequest
import org.p2p.wallet.history.api.RpcHistoryServiceApi
import org.p2p.wallet.history.interactor.mapper.RpcHistoryTransactionConverter
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.model.rpc.RpcHistoryTransaction
import org.p2p.wallet.history.signature.HistoryServiceSignatureFieldGenerator
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import java.util.Optional

private const val REQUEST_PARAMS_USER_ID = "user_id"
private const val REQUEST_PARAMS_LIMIT = "limit"
private const val REQUEST_PARAMS_OFFSET = "offset"
private const val REQUEST_PARAMS_SIGNATURE = "signature"
private const val REQUEST_PARAMS_NAME = "get_transactions"

class RpcHistoryRepository(
    private val historyApi: RpcHistoryServiceApi,
    private val tokenKeyProvider: TokenKeyProvider,
    private val historyServiceSignatureFieldGenerator: HistoryServiceSignatureFieldGenerator,
    private val converter: RpcHistoryTransactionConverter
) : HistoryRemoteRepository {

    private val allTransactions = mutableListOf<RpcHistoryTransaction>()

    override suspend fun loadHistory(limit: Int): List<RpcHistoryTransaction> {

        val signature = historyServiceSignatureFieldGenerator.generateSignature(
            pubKey = tokenKeyProvider.publicKey,
            privateKey = tokenKeyProvider.keyPair,
            offset = allTransactions.size.toLong(),
            limit = limit.toLong(),
            mint = Optional.empty()
        )
        val requestParams = mapOf(
            REQUEST_PARAMS_USER_ID to tokenKeyProvider.publicKey,
            REQUEST_PARAMS_LIMIT to limit,
            REQUEST_PARAMS_OFFSET to allTransactions.size.toLong(),
            REQUEST_PARAMS_SIGNATURE to signature
        )
        val rpcRequest = RpcMapRequest(
            method = REQUEST_PARAMS_NAME,
            params = requestParams
        )

        val response = historyApi.getTransactionHistory(rpcRequest).result
        val newTransactions = converter.toDomain(response)
        if (!allTransactions.containsAll(newTransactions)) {
            allTransactions.addAll(newTransactions)
        }
        return newTransactions
    }

    override fun findTransactionById(id: String): HistoryTransaction? {
        return allTransactions.firstOrNull { it.getHistoryTransactionId() == id }
    }
}
