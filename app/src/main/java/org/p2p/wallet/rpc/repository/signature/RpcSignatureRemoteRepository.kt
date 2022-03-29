package org.p2p.wallet.rpc.repository.signature

import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.model.types.ConfigObjects
import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.solanaj.model.types.SignatureInformationResponse
import org.p2p.wallet.rpc.api.RpcSignatureApi

class RpcSignatureRemoteRepository(private val rpcPoolApi: RpcSignatureApi) : RpcSignatureRepository {

    override suspend fun getConfirmedSignaturesForAddress(
        userAccountAddress: PublicKey,
        before: String?,
        limit: Int
    ): List<SignatureInformationResponse> {
        val params = listOf(
            userAccountAddress.toString(),
            ConfigObjects.ConfirmedSignFAddr2(before, limit)
        )

        val rpcRequest = RpcRequest("getConfirmedSignaturesForAddress2", params)
        return rpcPoolApi.getConfirmedSignaturesForAddress2(rpcRequest).result
    }
}
