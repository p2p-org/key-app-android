package org.p2p.wallet.renbtc.repository

import org.p2p.wallet.renbtc.model.RenBTCPayment
import org.p2p.wallet.renbtc.api.RenBTCApi
import org.p2p.solanaj.rpc.Environment

class RenRemoteRepository(
    private val api: RenBTCApi
) : RenRepository {

    override suspend fun getPaymentData(environment: Environment, gateway: String): List<RenBTCPayment> {
        val response = when (environment) {
            Environment.RPC_POOL,
            Environment.SOLANA,
            Environment.MAINNET -> api.getPaymentData(gateway)
            Environment.DEVNET -> api.getPaymentData("testnet", gateway)
        }
        return response.map { RenBTCPayment(it.transactionHash, it.txIndex, it.amount) }
    }
}
