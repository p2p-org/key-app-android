package org.p2p.wallet.renbtc.repository

import org.p2p.wallet.renbtc.model.RenBTCPayment
import org.p2p.solanaj.rpc.Environment

interface RenBTCRepository {
    suspend fun getPaymentData(environment: Environment, gateway: String): List<RenBTCPayment>
}