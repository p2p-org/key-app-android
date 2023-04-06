package org.p2p.ethereumkit.external.price

import java.math.BigDecimal

private const val USD_READABLE_SYMBOL = "usd"

interface PriceRepository {

    suspend fun getPriceForTokens(
        tokenAddresses: List<String>,
        targetCurrency: String = USD_READABLE_SYMBOL,
    ): Map<String, BigDecimal>

    suspend fun getPriceForToken(
        tokenAddress: String,
        targetCurrency: String = USD_READABLE_SYMBOL,
    ): BigDecimal
}
