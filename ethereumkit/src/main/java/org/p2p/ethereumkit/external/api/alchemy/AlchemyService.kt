package org.p2p.ethereumkit.external.api.alchemy

import org.p2p.ethereumkit.internal.api.core.RpcResponse
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Url
import java.net.URI

internal interface AlchemyService {
    @POST
    @Headers("Content-Type: application/json","Accept: application/json")
    suspend fun launch(@Url uri: URI, @Body jsonRpc: String): RpcResponse
}

