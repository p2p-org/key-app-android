package org.p2p.wallet.claim.repository

import org.p2p.solanaj.model.types.RpcMapRequest
import org.p2p.wallet.claim.api.BridgeApi
import org.p2p.wallet.claim.api.response.FeesResponse

class EthereumClaimRemoteRepository(private val api: BridgeApi) : EthereumClaimRepository {

    override suspend fun getEthereumFees(
        ethereumAddress: String,
        solanaAddress: String,
        tokenAddress: String?,
        amountAsString: String
    ): FeesResponse {

        val params = hashMapOf<String, Any?>(
            "user_wallet" to ethereumAddress,
            "recipient" to solanaAddress,
            "token" to tokenAddress,
            "amount" to amountAsString
        )
        val rpcRequest = RpcMapRequest("get_ethereum_fees", params)
        return api.getEthereumFees(rpcRequest).result
    }
}
