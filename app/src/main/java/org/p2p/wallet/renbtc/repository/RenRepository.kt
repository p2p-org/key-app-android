package org.p2p.wallet.renbtc.repository

import org.p2p.wallet.renbtc.model.RenBTCPayment
import org.p2p.solanaj.rpc.NetworkEnvironment

interface RenRepository {
    suspend fun getPaymentData(environment: NetworkEnvironment, gateway: String): List<RenBTCPayment>
}
