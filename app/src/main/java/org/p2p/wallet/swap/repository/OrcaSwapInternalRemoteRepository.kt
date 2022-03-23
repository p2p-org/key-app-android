package org.p2p.wallet.swap.repository

import org.p2p.wallet.infrastructure.network.environment.EnvironmentManager
import org.p2p.wallet.swap.api.InternalWebApi
import org.p2p.wallet.swap.model.orca.OrcaAquafarms
import org.p2p.wallet.swap.model.orca.OrcaConverter
import org.p2p.wallet.swap.model.orca.OrcaPools
import org.p2p.wallet.swap.model.orca.OrcaProgramId
import org.p2p.wallet.swap.model.orca.OrcaTokens
import org.p2p.solanaj.rpc.Environment

class OrcaSwapInternalRemoteRepository(
    private val webApi: InternalWebApi,
    private val environmentManager: EnvironmentManager
) : OrcaSwapInternalRepository {

    override suspend fun getTokens(): OrcaTokens {
        val response = webApi.loadTokens(getNetwork())
        return response.mapValues { OrcaConverter.fromNetwork(it.value) } as OrcaTokens
    }

    override suspend fun getAquafarms(): OrcaAquafarms {
        val response = webApi.loadAquafarms(getNetwork())
        return response.mapValues { OrcaConverter.fromNetwork(it.value) } as OrcaAquafarms
    }

    override suspend fun getPools(): OrcaPools {
        val response = webApi.loadPools(getNetwork())
        return response.mapValues { OrcaConverter.fromNetwork(it.value) } as OrcaPools
    }

    override suspend fun getProgramID(): OrcaProgramId {
        val response = webApi.loadProgramId(getNetwork())
        return OrcaProgramId(
            serumTokenSwap = response.serumTokenSwap,
            tokenSwapV2 = response.tokenSwapV2,
            tokenSwap = response.tokenSwap,
            token = response.token,
            aquafarm = response.aquafarm
        )
    }

    private fun getNetwork(): String =
        when (environmentManager.loadEnvironment()) {
            Environment.SOLANA,
            Environment.MAINNET,
            Environment.RPC_POOL -> "mainnet"
            else -> "mainnet" // fixme: add valid network type
        }
}
