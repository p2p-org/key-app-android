package org.p2p.wallet.main.repository

import org.p2p.wallet.main.model.MoonpayBuyResult
import java.math.BigDecimal

interface MoonpayRepository {

    suspend fun getCurrency(
        baseCurrencyAmount: String,
        quoteCurrencyCode: String,
        baseCurrencyCode: String
    ): MoonpayBuyResult

    suspend fun getCurrencyAskPrice(quoteCurrencyCode: String): BigDecimal
}