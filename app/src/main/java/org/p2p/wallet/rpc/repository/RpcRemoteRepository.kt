package org.p2p.wallet.rpc.repository

import org.p2p.solanaj.rpc.Environment
import org.p2p.wallet.rpc.api.RpcApi
import org.p2p.wallet.infrastructure.network.environment.EnvironmentManager

// TODO: Split to domain repositories
class RpcRemoteRepository(
    private val serumApi: RpcApi,
    private val mainnetApi: RpcApi,
    private val rpcpoolRpcApi: RpcApi,
    private val testnetApi: RpcApi,
    environmentManager: EnvironmentManager,
    onlyMainnet: Boolean = false
) : RpcRepository {

    private var rpcApi: RpcApi

    init {
        if (onlyMainnet) {
            rpcApi = mainnetApi
        } else {
            rpcApi = createRpcApi(environmentManager.loadEnvironment())
            environmentManager.setOnEnvironmentListener { rpcApi = createRpcApi(it) }
        }
    }

    private fun createRpcApi(environment: Environment): RpcApi = when (environment) {
        Environment.SOLANA -> serumApi
        Environment.RPC_POOL -> rpcpoolRpcApi
        Environment.MAINNET -> mainnetApi
        Environment.DEVNET -> testnetApi
    }

    /**
     * The history is being fetched from main-net despite the selected network
     * */
}