package org.p2p.wallet.rpc.repository.balance

import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.solanaj.model.types.TokenAccountBalance
import org.p2p.solanaj.model.types.TokenSupply
import org.p2p.wallet.rpc.api.RpcBalanceApi
import java.math.BigInteger

class RpcBalanceRemoteRepository(private val rpcApi: RpcBalanceApi) : RpcBalanceRepository {

    override suspend fun getBalance(account: PublicKey): Long {
        val params = listOf(account.toBase58())
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

    override suspend fun getTokenAccountBalances(accounts: List<String>): List<Pair<String, TokenAccountBalance>> {
        val requestsBatch = accounts.map {
            val params = listOf(it)
            RpcRequest("getTokenAccountBalance", params)
        }

        return rpcApi.getTokenAccountBalances(requestsBatch)
            .mapIndexed { index, response ->
                requestsBatch[index].params!!.first() as String to response.result
            }
    }

    override suspend fun getTokenSupply(mint: String): TokenSupply {
        val params = listOf(mint)
        val rpcRequest = RpcRequest("getTokenSupply", params)
        return rpcApi.getTokenSupply(rpcRequest).result
    }

    override suspend fun getTokenAccountBalance(account: PublicKey): TokenAccountBalance {
        val params = listOf(account.toString())
        val rpcRequest = RpcRequest("getTokenAccountBalance", params)
        return rpcApi.getTokenAccountBalance(rpcRequest).result
    }
}
