package org.p2p.wallet.rpc.repository.blockhash

import org.p2p.solanaj.model.types.RecentBlockhash
import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.wallet.rpc.api.RpcBlockHashApi

class RpcBlockHashRemoteRepository(private val rpcApi: RpcBlockHashApi) : RpcBlockHashRepository {

    override suspend fun getRecentBlockhash(): RecentBlockhash {
        val rpcRequest = RpcRequest("getRecentBlockhash", null)
        return rpcApi.getRecentBlockhash(rpcRequest).result
    }
}