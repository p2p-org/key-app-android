package org.p2p.wallet.rpc.repository.transaction

import android.util.Base64
import org.p2p.solanaj.core.Transaction
import org.p2p.solanaj.kits.transaction.ConfirmedTransactionParsed
import org.p2p.solanaj.model.types.Encoding
import org.p2p.solanaj.model.types.RequestConfiguration
import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.wallet.rpc.api.RpcTransactionApi

class RpcTransactionRemoteRepository(
    private val rpcApi: RpcTransactionApi,
    private val rpcPoolApi: RpcTransactionApi
) : RpcTransactionRepository {

    override suspend fun sendTransaction(transaction: Transaction): String {
        val serializedTransaction = transaction.serialize()

        val base64Trx = Base64
            .encodeToString(serializedTransaction, Base64.DEFAULT)
            .replace("\n", "")

        val params = mutableListOf<Any>()

        params.add(base64Trx)
        params.add(RequestConfiguration(encoding = Encoding.BASE64.encoding))

        val rpcRequest = RpcRequest("sendTransaction", params)
        return rpcApi.sendTransaction(rpcRequest).result
    }

    override suspend fun simulateTransaction(transaction: Transaction): String {
        val serializedTransaction = transaction.serialize()

        val base64Trx = Base64
            .encodeToString(serializedTransaction, Base64.DEFAULT)
            .replace("\n", "")

        val params = mutableListOf<Any>()

        params.add(base64Trx)
        params.add(RequestConfiguration(encoding = Encoding.BASE64.encoding))

        val rpcRequest = RpcRequest("simulateTransaction", params)
        val result = rpcApi.simulateTransaction(rpcRequest).result
        if (result.value.error != null) {
            throw IllegalStateException("Transaction simulation failed: ${result.value.linedLogs()}")
        } else return ""
    }

    override suspend fun sendTransaction(serializedTransaction: String): String {
        val base64Trx = serializedTransaction.replace("\n", "")

        val params = mutableListOf<Any>()
        params.add(base64Trx)
        params.add(RequestConfiguration(encoding = Encoding.BASE64.encoding))

        val rpcRequest = RpcRequest("sendTransaction", params)
        return rpcApi.sendTransaction(rpcRequest).result
    }

    override suspend fun simulateTransaction(serializedTransaction: String): String {
        val base64Trx = serializedTransaction.replace("\n", "")

        val params = mutableListOf<Any>()
        params.add(base64Trx)
        params.add(RequestConfiguration(encoding = Encoding.BASE64.encoding))

        val rpcRequest = RpcRequest("simulateTransaction", params)
        val result = rpcApi.simulateTransaction(rpcRequest).result
        if (result.value.error != null) {
            throw IllegalStateException("Transaction simulation failed: ${result.value.linedLogs()}")
        } else return ""
    }

    override suspend fun getConfirmedTransactions(
        signatures: List<String>
    ): List<ConfirmedTransactionParsed> {
        val requestsBatch = signatures.map {
            val encoding = mapOf("encoding" to "jsonParsed")
            val params = listOf(it, encoding)

            RpcRequest("getConfirmedTransaction", params)
        }

        return rpcPoolApi.getConfirmedTransactions(requestsBatch).map { it.result }
    }
}