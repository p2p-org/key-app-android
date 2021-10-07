package com.p2p.wallet.auth.api

import retrofit2.http.GET
import retrofit2.http.POST

interface GeeTestApi {

    @GET()
    suspend fun getCaptcha(): GetCaptchaResponse

    @POST()
    suspend fun sendCaptcha(): String
}