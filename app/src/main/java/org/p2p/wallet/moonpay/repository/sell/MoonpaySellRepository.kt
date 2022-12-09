package org.p2p.wallet.moonpay.repository.sell

import org.p2p.wallet.moonpay.model.MoonpaySellError
import org.p2p.wallet.moonpay.model.MoonpaySellTransaction
import org.p2p.wallet.utils.Base58String

interface MoonpaySellRepository {
    fun isSellAllowedForUser(): Boolean

    suspend fun loadMoonpayFlags()
    @Throws(MoonpaySellError::class)
    suspend fun getUserSellTransactions(userAddress: Base58String): List<MoonpaySellTransaction>
}
