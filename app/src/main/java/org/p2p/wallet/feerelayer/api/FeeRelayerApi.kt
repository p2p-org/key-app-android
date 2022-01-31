package org.p2p.wallet.feerelayer.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * For now, V2 endpoints are working for Devnet network
 * The endpoints without v2 prefix are used for Mainnet network
 * */
interface FeeRelayerApi {

    @GET("fee_payer/pubkey")
    suspend fun getPublicKey(): String

    @POST("relay_transaction")
    suspend fun relayTransaction(@Body request: SendTransactionRequest): List<String>

    @POST("relay_top_up_with_swap")
    suspend fun relayTopUpSwap(@Body request: RelayTopUpSwapRequest): List<String>

    @POST("relay_swap")
    suspend fun relaySwap(@Body request: RelaySwapRequest): List<String>

    @POST("relay_transfer_spl_token")
    suspend fun relayTransferSplToken(@Body request: RelayTransferRequest): List<String>

    // region devnet endpoints
    @GET("v2/fee_payer/pubkey")
    suspend fun getPublicKeyV2(): String

    @POST("v2/relay_transaction")
    suspend fun relayTransactionV2(@Body request: SendTransactionRequest): List<String>

    @POST("v2/relay_top_up_with_swap")
    suspend fun relayTopUpSwapV2(@Body request: RelayTopUpSwapRequest): List<String>

    @POST("v2/relay_swap")
    suspend fun relaySwapV2(@Body request: RelaySwapRequest): List<String>

    @POST("v2/relay_transfer_spl_token")
    suspend fun relayTransferSplTokenV2(@Body request: RelayTransferRequest): List<String>
}