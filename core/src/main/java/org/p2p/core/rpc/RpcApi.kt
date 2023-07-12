package org.p2p.core.rpc

import com.google.gson.JsonObject
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Url
import java.net.URI

const val RPC_RETROFIT_QUALIFIER = "RPC_RETROFIT_QUALIFIER"
const val RPC_JSON_QUALIFIER = "RPC_JSON_QUALIFIER"

interface RpcApi {
    @POST
    @Headers("Content-Type: application/json", "Accept: application/json")
    suspend fun launch(@Url uri: URI, @Body jsonRpc: String): RpcResponse

    @GET
    @Headers("Content-Type: application/json", "Accept: application/json")
    suspend fun getZipFile(@Url uri: String, @Header("last-modified") lastModified: String?): JsonObject
}
