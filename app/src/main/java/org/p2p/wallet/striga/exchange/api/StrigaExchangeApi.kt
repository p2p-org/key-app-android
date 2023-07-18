package org.p2p.wallet.striga.exchange.api

import retrofit2.http.POST
import org.p2p.wallet.striga.exchange.api.response.StrigaExchangeRateItemResponse

interface StrigaExchangeApi {

    @POST("v1/trade/rates")
    suspend fun getExchangeRates(): Map<String, StrigaExchangeRateItemResponse>
}
