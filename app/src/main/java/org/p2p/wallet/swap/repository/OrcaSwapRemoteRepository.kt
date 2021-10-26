package org.p2p.wallet.swap.repository

import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.ws.NotificationEventListener
import org.p2p.solanaj.ws.SubscriptionWebSocketClient
import org.p2p.wallet.infrastructure.network.environment.EnvironmentManager
import org.p2p.wallet.rpc.repository.RpcRepository
import org.p2p.wallet.swap.model.AccountBalance

class OrcaSwapRemoteRepository(
    private val rpcRepository: RpcRepository,
    private val environmentManager: EnvironmentManager
) : OrcaSwapRepository {

    override suspend fun loadTokenBalance(publicKey: PublicKey): AccountBalance {
        val response = rpcRepository.getTokenAccountBalance(publicKey)
        return AccountBalance(publicKey, response.amount, response.value.decimals)
    }

    override suspend fun sendAndWait(serializedTransaction: String, onConfirmed: () -> Unit) {
        val signature = rpcRepository.sendTransaction(serializedTransaction)

        val listener = NotificationEventListener { onConfirmed() }

        val endpoint = environmentManager.loadEnvironment().endpoint
        val wssClient = SubscriptionWebSocketClient.getInstance(endpoint)
        wssClient.signatureSubscribe(signature, listener)
    }
}