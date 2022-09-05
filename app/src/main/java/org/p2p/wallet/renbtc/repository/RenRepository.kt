package org.p2p.wallet.renbtc.repository

import org.p2p.wallet.renbtc.model.RenBTCPayment
import org.p2p.wallet.infrastructure.network.environment.NetworkEnvironment

interface RenRepository {
    suspend fun getPaymentData(environment: NetworkEnvironment, gateway: String): List<RenBTCPayment>
}
