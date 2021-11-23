package org.p2p.wallet.swap.repository

import org.p2p.solanaj.core.PublicKey
import org.p2p.wallet.rpc.repository.RpcRepository
import org.p2p.wallet.swap.model.AccountBalance
import org.p2p.wallet.updates.UpdatesManager

class OrcaSwapRemoteRepository(
    private val rpcRepository: RpcRepository,
    private val updatesManager: UpdatesManager
) : OrcaSwapRepository {

    override suspend fun loadTokenBalance(publicKey: PublicKey): AccountBalance {
        val response = rpcRepository.getTokenAccountBalance(publicKey)
        return AccountBalance(publicKey, response.amount, response.value.decimals)
    }

    override suspend fun sendAndWait(serializedTransaction: String) {
        val signature = rpcRepository.sendTransaction(serializedTransaction)
        updatesManager.subscribeToTransaction(signature)
    }
}