package org.p2p.wallet.rpc.repository.history

import org.p2p.solanaj.core.Transaction
import org.p2p.solanaj.model.types.Encoding
import org.p2p.solanaj.model.types.RequestConfiguration
import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.solanaj.utils.crypto.Base64Utils
import org.p2p.wallet.rpc.api.RpcHistoryApi
import org.p2p.wallet.utils.emptyString

class RpcHistoryRemoteRepository(
    private val rpcApi: RpcHistoryApi
) : RpcHistoryRepository {

    override suspend fun sendTransaction(transaction: Transaction): String {
        val serializedTransaction = transaction.serialize()

        val base64Transaction = Base64Utils.encode(serializedTransaction)
            .replace("\n", emptyString())

        val params = buildList {
            add(base64Transaction)
            add(RequestConfiguration(encoding = Encoding.BASE64.encoding))
        }
        val rpcRequest = RpcRequest("sendTransaction", params)
        return rpcApi.sendTransaction(rpcRequest).result
    }

    override suspend fun simulateTransaction(transaction: Transaction): String {
        val serializedTransaction = transaction.serialize()

        val base64Transaction = Base64Utils.encode(serializedTransaction)
            .replace("\n", emptyString())

        val params = buildList<Any> {
            add(base64Transaction)
            add(RequestConfiguration(encoding = Encoding.BASE64.encoding))
        }

        val rpcRequest = RpcRequest("simulateTransaction", params)
        val result = rpcApi.simulateTransaction(rpcRequest).result
        if (result.value.error != null) {
            throw IllegalStateException("Transaction simulation failed: ${result.value.linedLogs()}")
        } else {
            return emptyString()
        }
    }

    override suspend fun sendTransaction(serializedTransaction: String): String {
        val base64Transaction = serializedTransaction.replace("\n", emptyString())

        val params = buildList<Any> {
            add(base64Transaction)
            add(RequestConfiguration(encoding = Encoding.BASE64.encoding))
        }

        val rpcRequest = RpcRequest("sendTransaction", params)
        return rpcApi.sendTransaction(rpcRequest).result
    }

    override suspend fun simulateTransaction(serializedTransaction: String): String {
        val base64Transaction = serializedTransaction.replace("\n", emptyString())

        val params = buildList<Any> {
            add(base64Transaction)
            add(RequestConfiguration(encoding = Encoding.BASE64.encoding))
        }

        val rpcRequest = RpcRequest("simulateTransaction", params)
        val result = rpcApi.simulateTransaction(rpcRequest).result
        if (result.value.error != null) {
            throw IllegalStateException("Transaction simulation failed: ${result.value.linedLogs()}")
        } else {
            return emptyString()
        }
    }
}
