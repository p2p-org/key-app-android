package org.p2p.wallet.swap.repository

import org.p2p.solanaj.core.PublicKey
import org.p2p.wallet.rpc.repository.token.RpcTokenRepository
import org.p2p.wallet.rpc.repository.transaction.RpcTransactionRepository
import org.p2p.wallet.swap.model.AccountBalance
import org.p2p.wallet.updates.UpdatesManager
import org.p2p.wallet.utils.toPublicKey

class OrcaSwapRemoteRepository(
    private val rpcTransactionRepository: RpcTransactionRepository,
    private val rpcTokenRepository: RpcTokenRepository,
    private val updatesManager: UpdatesManager
) : OrcaSwapRepository {

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