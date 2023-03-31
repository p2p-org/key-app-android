package org.p2p.wallet.swap.repository

import org.p2p.solanaj.core.PublicKey
import org.p2p.wallet.rpc.repository.balance.RpcBalanceRepository
import org.p2p.wallet.rpc.repository.history.RpcTransactionRepository
import org.p2p.wallet.swap.api.OrcaApi
import org.p2p.wallet.swap.model.AccountBalance
import org.p2p.wallet.swap.model.orca.OrcaConfigs
import org.p2p.wallet.swap.model.orca.OrcaConverter
import org.p2p.wallet.updates.UpdatesManager
import org.p2p.wallet.utils.toPublicKey
import timber.log.Timber

class OrcaSwapRemoteRepository(
    private val orcaApi: OrcaApi,
    private val rpcTransactionRepository: RpcTransactionRepository,
    private val rpcTokenRepository: RpcBalanceRepository,
    private val updatesManager: UpdatesManager
) : OrcaSwapRepository {

    private var configs: OrcaConfigs? = null

    override suspend fun loadOrcaConfigs(): OrcaConfigs {
        if (configs != null) return configs!!

        val response = orcaApi.loadConfigs()
        return OrcaConverter.fromNetwork(response).also {
            Timber.tag("OrcaApi").d("Returning cached config value")
            configs = it
        }
    }

    override suspend fun loadTokenBalances(publicKeys: List<String>): List<Pair<String, AccountBalance>> {
        val response = rpcTokenRepository.getTokenAccountBalances(publicKeys)
        return response.map { (publicKey, balance) ->
            publicKey to AccountBalance(publicKey.toPublicKey(), balance.amount, balance.value.decimals)
        }
    }

    override suspend fun loadTokenBalance(publicKey: PublicKey): AccountBalance {
        val response = rpcTokenRepository.getTokenAccountBalance(publicKey)
        return AccountBalance(publicKey, response.amount, response.value.decimals)
    }

    override suspend fun sendAndWait(serializedTransaction: String) {
        val signature = rpcTransactionRepository.sendTransaction(serializedTransaction)
        updatesManager.subscribeToTransaction(signature)
    }
}
