package com.p2p.wallet.auth.api

import com.p2p.wallet.auth.model.NameRegisterBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UsernameApi {

    @GET("name_register/{username}")
    suspend fun usernameCheck(@Path("username") username: String): UsernameCheckResponse

    @POST("name_register/{username}")
    suspend fun usernameRegister(@Body body: NameRegisterBody): String
}