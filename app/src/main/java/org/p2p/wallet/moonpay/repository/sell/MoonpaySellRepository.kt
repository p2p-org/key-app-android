package org.p2p.wallet.moonpay.repository.sell

interface MoonpaySellRepository {
    suspend fun isSellAllowedForUser(): Boolean
    suspend fun loadMoonpayFlags()
}
