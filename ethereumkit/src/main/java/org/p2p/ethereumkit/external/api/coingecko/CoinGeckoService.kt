package org.p2p.ethereumkit.external.api.coingecko

import org.p2p.ethereumkit.external.api.coingecko.response.CoinGeckoPriceResponse
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

internal interface CoinGeckoService {

    @GET
    suspend fun getEthereumTokenPrices(
        @Url url: String = "https://api.coingecko.com/api/v3/simple/token_price/ethereum",
        @Query("contract_addresses") tokenAddresses: String,
        @Query("vs_currencies") targetCurrency: String,
    ): Map<String, CoinGeckoPriceResponse>
}
