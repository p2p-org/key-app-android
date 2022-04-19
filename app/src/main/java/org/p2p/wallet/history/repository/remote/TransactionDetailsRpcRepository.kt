package org.p2p.wallet.history.repository.remote

import kotlinx.coroutines.withContext
import org.p2p.solanaj.kits.transaction.TransactionDetails
import org.p2p.solanaj.kits.transaction.mapper.TransactionDetailsNetworkMapper
import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.rpc.RpcConstants
import org.p2p.wallet.rpc.api.RpcHistoryApi

class TransactionDetailsRpcRepository(
    private val rpcApi: RpcHistoryApi,
    private val dispatchers: CoroutineDispatchers,
    private val transactionDetailsNetworkMapper: TransactionDetailsNetworkMapper,
) : TransactionDetailsRemoteRepository {

    override suspend fun getTransactions(signatures: List<String>): List<TransactionDetails> {
        val requestsBatch = signatures.map { signature ->
            val encoding = buildMap {
                this[RpcConstants.REQUEST_PARAMETER_KEY_ENCODING] =
                    RpcConstants.REQUEST_PARAMETER_VALUE_JSON_PARSED
            }
            val params = listOf(signature, encoding)

            RpcRequest(method = RpcConstants.REQUEST_METHOD_VALUE_GET_CONFIRMED_TRANSACTIONS, params = params)
        }

        return rpcApi.getConfirmedTransactions(requestsBatch)
            .map { it.result }
            .let {
                withContext(dispatchers.io) {
                    transactionDetailsNetworkMapper.fromNetworkToDomain(it)
                }
            }
    }
}
