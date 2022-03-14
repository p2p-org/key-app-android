package org.p2p.wallet.rpc.repository.pool

import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.kits.Pool
import org.p2p.solanaj.model.types.ConfigObjects
import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.solanaj.model.types.RpcSendTransactionConfig
import org.p2p.wallet.rpc.api.RpcApi

class RpcPoolRemoteRepository(private val rpcApi: RpcApi) : RpcPoolRepository {

    override suspend fun getPools(account: PublicKey): List<Pool.PoolInfo> {
        val params = listOf(
            account.toString(),
            ConfigObjects.ProgramAccountConfig(RpcSendTransactionConfig.Encoding.base64)
        )
        val rpcRequest = RpcRequest("getProgramAccounts", params)
        val response = rpcApi.getProgramAccounts(rpcRequest).result
        return response.map { Pool.PoolInfo.fromProgramAccount(it) }
    }
}