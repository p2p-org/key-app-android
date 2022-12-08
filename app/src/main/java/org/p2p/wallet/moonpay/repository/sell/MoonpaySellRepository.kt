package org.p2p.wallet.moonpay.repository.sell

interface MoonpaySellRepository {
    fun isSellAllowedForUser(): Boolean
    suspend fun loadMoonpayFlags()
}
