package org.p2p.wallet.auth.api

import com.google.gson.JsonObject
import org.p2p.wallet.auth.model.RegisterNameRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UsernameApi {

    @GET("name_register/{username}")
    suspend fun checkUsername(@Path("username") username: String): CheckUsernameResponse

    @GET("name_register/auth/gt/register")
    suspend fun checkCaptcha(): JsonObject

    @POST("name_register/{username}")
    suspend fun registerUsername(
        @Path("username") username: String,
        @Body body: RegisterNameRequest
    )

    @GET("name_register/lookup/{owner}")
    suspend fun lookup(@Path("owner") owner: String): ArrayList<LookupUsernameResponse>

    @GET("name_register/resolve/{name}")
    suspend fun resolve(@Path("name") name: String): List<ResolveUsernameResponse>
}
