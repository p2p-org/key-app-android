package com.p2p.wallet.auth.api

import com.p2p.wallet.auth.model.NameRegisterBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UsernameApi {

    @GET("name_register/{username}")
    suspend fun checkUsername(@Path("username") username: String): UsernameCheckResponse

    @GET("name_register/auth/gt/register")
    suspend fun checkCaptcha(): GetCaptchaResponse

    @POST("name_register/")
    suspend fun registerUsername(@Body body: NameRegisterBody): String
}