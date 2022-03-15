package org.p2p.wallet.rpc.repository.amount

import org.p2p.solanaj.model.types.RequestConfiguration
import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.wallet.rpc.api.RpcAmountApi
import java.math.BigInteger

class RpcAmountRemoteRepository(
    private val rpcApi: RpcAmountApi
) : RpcAmountRepository {

    override suspend fun getFees(commitment: String?): BigInteger {
        val params = commitment?.let {
            val config = RequestConfiguration(commitment = it)
            listOf(config)
        }
        val rpcRequest = RpcRequest("getFees", params)
        val response = rpcApi.getFees(rpcRequest).result
        return BigInteger.valueOf(response.value.feeCalculator.lamportsPerSignature)
    }
}