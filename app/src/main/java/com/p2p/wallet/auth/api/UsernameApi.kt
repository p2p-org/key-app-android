package com.p2p.wallet.auth.api

import com.p2p.wallet.auth.model.NameRegisterBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UsernameApi {

    @GET("name_register/{username}")
    suspend fun checkUsername(@Path("username") username: String): CheckUsernameResponse

    @GET("name_register/auth/gt/register")
    suspend fun checkCaptcha(): CheckCaptchaResponse

    @POST("name_register/{username}")
    suspend fun registerUsername(@Path("username") username: String, @Body body: NameRegisterBody): RegisterUsernameResponse
}