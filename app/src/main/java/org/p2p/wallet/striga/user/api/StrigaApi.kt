package org.p2p.wallet.striga.user.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import org.p2p.wallet.striga.user.api.response.StrigaCreateUserResponse
import org.p2p.wallet.striga.user.api.response.StrigaUserDetailsResponse

interface StrigaApi {
    @POST("v1/user/create")
    suspend fun createUser(@Body body: StrigaCreateUserRequest): StrigaCreateUserResponse

    @GET("v1/user/{userId}")
    suspend fun getUserDetails(@Path("userId") userId: String): StrigaUserDetailsResponse

    /**
     * 30044 - mobile already in use
     * 30031 - invalid verification code
     * 30003 - exceeded verification attempts
     * 30005 - user doesn't exist, critical error
     */
    @POST("v1/user/verify-mobile")
    suspend fun verifyMobileNumber(@Body body: StrigaVerifyMobileNumberRequest)

    /**
     * 31009 - mobile already in use
     * 31008 - exceed resend sms attempts
     */
    @POST("v1/user/resend-sms")
    suspend fun resendSms(@Body body: StrigaResendSmsRequest)
}
