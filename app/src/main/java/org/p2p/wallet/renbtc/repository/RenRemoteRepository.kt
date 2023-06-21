package org.p2p.wallet.renbtc.repository

import org.p2p.core.network.environment.NetworkEnvironment
import org.p2p.wallet.renbtc.api.RenBTCApi
import org.p2p.wallet.renbtc.model.RenBTCPayment

class RenRemoteRepository(
    private val api: RenBTCApi
) : RenRepository {

    override suspend fun getPaymentData(environment: NetworkEnvironment, gateway: String): List<RenBTCPayment> {
        val response = when (environment) {
            NetworkEnvironment.RPC_POOL,
            NetworkEnvironment.SOLANA,
            NetworkEnvironment.MAINNET -> api.getPaymentData(gateway)
            NetworkEnvironment.DEVNET -> api.getPaymentData("testnet", gateway)
        }
        return response.map { RenBTCPayment(it.transactionHash, it.txIndex, it.amount) }
    }
}
