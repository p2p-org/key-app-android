package org.p2p.wallet.striga.user.api

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import org.p2p.wallet.striga.user.api.response.StrigaUserDetailsResponse

interface StrigaApi {
    @POST("v1/user/{userId}")
    suspend fun getUserDetails(@Path("userId") userId: String): StrigaUserDetailsResponse

    @POST("v1/user/verify-mobile")
    suspend fun verifyMobileNumber(@Body body: StrigaVerifyMobileNumberRequest)
}
