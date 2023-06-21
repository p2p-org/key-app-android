package org.p2p.wallet.renbtc.repository

import org.p2p.core.network.environment.NetworkEnvironment
import org.p2p.wallet.renbtc.model.RenBTCPayment

interface RenRepository {
    suspend fun getPaymentData(environment: NetworkEnvironment, gateway: String): List<RenBTCPayment>
}
