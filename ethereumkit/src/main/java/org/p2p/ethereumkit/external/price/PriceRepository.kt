package org.p2p.ethereumkit.external.price

import org.p2p.ethereumkit.external.api.coingecko.response.CoinGeckoPriceResponse

private const val USD_READABLE_SYMBOL = "usd"

internal interface PriceRepository {

    suspend fun getTokenPrice(
        tokenAddresses: List<String>,
        targetCurrency: String = USD_READABLE_SYMBOL
    ): Map<String, CoinGeckoPriceResponse>
}
