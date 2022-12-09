package org.p2p.wallet.moonpay.serversideapi

import org.p2p.wallet.moonpay.serversideapi.response.MoonpaySellTransactionResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MoonpayServerSideApi {
    @GET("v3/sell_transactions")
    suspend fun getUserSellTransactions(
        @Query("customerId") userAddress: String
    ): List<MoonpaySellTransactionResponse>
}
