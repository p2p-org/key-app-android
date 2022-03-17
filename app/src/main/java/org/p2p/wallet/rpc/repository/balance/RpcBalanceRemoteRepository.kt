package org.p2p.wallet.rpc.repository.balance

import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.wallet.rpc.api.RpcBalanceApi
import java.math.BigInteger

class RpcBalanceRemoteRepository(private val rpcApi: RpcBalanceApi) : RpcBalanceRepository {

    override suspend fun getBalance(account: String): Long {
        val params = listOf(account)
        val rpcRequest = RpcRequest("getBalance", params)
        return rpcApi.getBalance(rpcRequest).result.value
    }

    override suspend fun getBalances(accounts: List<String>): List<Pair<String, BigInteger>> {
        val requestsBatch = accounts.map {
            val params = listOf(it)
            RpcRequest("getBalance", params)
        }

        return rpcApi
            .getBalances(requestsBatch)
            .mapIndexed { index, response ->
                requestsBatch[index].params!!.first() as String to response.result.value.toBigInteger()
            }
    }
}