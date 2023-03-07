package org.p2p.ethereumkit.external.price

import org.p2p.ethereumkit.external.api.coingecko.CoinGeckoService
import org.p2p.ethereumkit.external.api.coingecko.response.CoinGeckoPriceResponse

internal class EthereumPriceRepository(
    private val priceApi: CoinGeckoService
): PriceRepository {


    override suspend fun getTokenPrice(
        tokenAddresses: List<String>,
        targetCurrency: String
    ): Map<String, CoinGeckoPriceResponse> {
        return priceApi.getEthereumTokenPrices(
            tokenAddresses = tokenAddresses.joinToString(","),
            targetCurrency = targetCurrency
        )
    }
}
