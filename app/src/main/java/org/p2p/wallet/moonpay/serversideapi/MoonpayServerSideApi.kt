package org.p2p.wallet.moonpay.serversideapi

import org.p2p.wallet.moonpay.serversideapi.response.MoonpaySellTransactionResponse
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Query

interface MoonpayServerSideApi {
    @GET("api/v3/sell_transactions")
    suspend fun getUserSellTransactions(
        @Query("externalCustomerId") externalCustomerId: String
    ): List<MoonpaySellTransactionResponse>

    /**
     * HTTP status 204 No Content if the sell transaction was successfully canceled.
     * If sell transaction could not be canceled (e.g. because it has already been completed)
     * it will return HTTP status 409 Conflict.
     */
    @DELETE("v3/sell_transactions/{transactionId}")
    suspend fun cancelSellTransaction(
        @Query("transactionId") sellTransactionId: String
    )
}
