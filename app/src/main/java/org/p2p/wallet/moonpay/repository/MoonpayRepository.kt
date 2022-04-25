package org.p2p.wallet.moonpay.repository

import org.p2p.wallet.moonpay.model.MoonpayBuyResult
import java.math.BigDecimal

interface MoonpayRepository {

    suspend fun getCurrency(
        baseCurrencyAmount: String?,
        quoteCurrencyAmount: String?,
        quoteCurrencyCode: String,
        baseCurrencyCode: String
    ): MoonpayBuyResult

    suspend fun getCurrencyAskPrice(quoteCurrencyCode: String): BigDecimal
}
