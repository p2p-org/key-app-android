package org.p2p.wallet.moonpay.serversideapi

import org.p2p.wallet.moonpay.serversideapi.response.MoonpaySellTransactionResponse
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Query

interface MoonpayServerSideApi {
    @GET("api/v3/sell_transactions")
    suspend fun getUserSellTransactions(
        @Query("externalCustomerId") userAddress: String
    ): List<MoonpaySellTransactionResponse>

    @DELETE("v3/sell_transactions/{transactionId}")
    suspend fun cancelSellTransaction(
        @Query("transactionId") sellTransactionId: String
    )
}
