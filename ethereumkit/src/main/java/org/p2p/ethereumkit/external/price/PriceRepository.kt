package org.p2p.ethereumkit.external.price

import java.math.BigDecimal

private const val USD_READABLE_SYMBOL = "usd"

internal interface PriceRepository {

    suspend fun getTokenPrices(
        tokenAddresses: List<String>,
        targetCurrency: String = USD_READABLE_SYMBOL
    ): Map<String, BigDecimal>
}
