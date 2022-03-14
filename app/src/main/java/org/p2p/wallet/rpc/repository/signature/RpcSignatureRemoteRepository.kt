package org.p2p.wallet.rpc.repository.signature

import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.model.types.ConfigObjects
import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.solanaj.model.types.SignatureInformation
import org.p2p.wallet.rpc.api.RpcApi

class RpcSignatureRemoteRepository(private val rpcPoolApi: RpcApi) : RpcSignatureRepository {

    override suspend fun getConfirmedSignaturesForAddress(
        account: PublicKey,
        before: String?,
        limit: Int
    ): List<SignatureInformation> {
        val params = listOf(
            account.toString(),
            ConfigObjects.ConfirmedSignFAddr2(before, limit)
        )

        val rpcRequest = RpcRequest("getConfirmedSignaturesForAddress2", params)
        return rpcPoolApi.getConfirmedSignaturesForAddress2(rpcRequest).result
    }
}