package org.p2p.wallet.striga.wallet.api

import retrofit2.http.Body
import retrofit2.http.POST
import org.p2p.wallet.striga.wallet.api.request.StrigaAddWhitelistedAddressRequest
import org.p2p.wallet.striga.wallet.api.request.StrigaEnrichAccountRequest
import org.p2p.wallet.striga.wallet.api.request.StrigaGetWhitelistedAddressesRequest
import org.p2p.wallet.striga.wallet.api.request.StrigaInitWithdrawalRequest
import org.p2p.wallet.striga.wallet.api.response.StrigaEnrichFiatAccountResponse
import org.p2p.wallet.striga.wallet.api.response.StrigaInitWithdrawalResponse
import org.p2p.wallet.striga.wallet.api.response.StrigaWhitelistedAddressItemResponse
import org.p2p.wallet.striga.wallet.api.response.StrigaWhitelistedAddressesResponse

interface StrigaWalletApi {

    @POST("v1/wallets/send/initiate/onchain")
    suspend fun initiateOnchainWithdrawal(
        @Body body: StrigaInitWithdrawalRequest
    ): StrigaInitWithdrawalResponse

    @POST("v1/wallets/get/whitelisted-addresses")
    suspend fun getWhitelistedAddresses(
        @Body body: StrigaGetWhitelistedAddressesRequest
    ): StrigaWhitelistedAddressesResponse

    @POST("v1/wallets/whitelist-address")
    suspend fun addWhitelistedAddress(
        @Body body: StrigaAddWhitelistedAddressRequest
    ): StrigaWhitelistedAddressItemResponse

    /**
     * This method returns absolutely different responses for crypto and fiat accounts
     * So use it for fiat accounts only
     */
    @POST("v1/wallets/account/enrich")
    suspend fun enrichFiatAccount(@Body body: StrigaEnrichAccountRequest): StrigaEnrichFiatAccountResponse
}
