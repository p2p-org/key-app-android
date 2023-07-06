package org.p2p.wallet.rpc.repository.ren

import org.p2p.solanaj.kits.renBridge.renVM.types.ParamsSubmitMint
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseQueryBlockState
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseQueryConfig
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseQueryTxMint
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseSubmitTxMint
import org.p2p.solanaj.model.types.RpcMapRequest
import org.p2p.solanaj.rpc.RenPoolRepository
import org.p2p.core.network.environment.NetworkEnvironmentManager

class RenPoolRemoteRepository(
    private val api: RenPoolApi,
    private val environmentManager: NetworkEnvironmentManager
) : RenPoolRepository {

    override suspend fun getQueryMint(txHash: String): ResponseQueryTxMint {
        val baseUrl = environmentManager.loadRpcEnvironment().lightNode
        val params = hashMapOf<String, Any>("txHash" to txHash)
        return api.queryMint(url = baseUrl, rpcRequest = RpcMapRequest(method = "ren_queryTx", params = params)).result
    }

    override suspend fun getQueryBlockState(): ResponseQueryBlockState {
        val baseUrl = environmentManager.loadRpcEnvironment().lightNode

        val rpcRequest = RpcMapRequest(method = "ren_queryBlockState", params = emptyMap())

        return api.queryBlockState(url = baseUrl, rpcRequest = rpcRequest).result
    }

    override suspend fun getQueryConfig(): ResponseQueryConfig {
        val baseUrl = environmentManager.loadRpcEnvironment().lightNode
        val rpcRequest = RpcMapRequest(method = "ren_queryConfig", params = emptyMap())

        return api.queryConfig(
            url = baseUrl, rpcReuest = rpcRequest
        ).result
    }

    override suspend fun submitTx(
        hash: String,
        mintTx: ParamsSubmitMint.MintTransactionInput,
        selector: String
    ): ResponseSubmitTxMint {

        val baseUrl = environmentManager.loadRpcEnvironment().lightNode
        val submitMint = ParamsSubmitMint(hash, mintTx, selector)
        val params = hashMapOf<String, Any>("tx" to submitMint)
        return api.submitTx(
            url = baseUrl, rpcRequest = RpcMapRequest(method = "ren_submitTx", params = params)
        ).result
    }
}
