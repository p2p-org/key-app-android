package org.p2p.wallet.auth.username.api

import org.p2p.wallet.auth.username.api.request.CreateNameRequest
import org.p2p.wallet.auth.username.api.request.GetNameRequest
import org.p2p.wallet.auth.username.api.request.LookupNameRequest
import org.p2p.wallet.auth.username.api.request.ResolveNameRequest
import org.p2p.wallet.auth.username.api.response.CreateNameResponse
import org.p2p.wallet.auth.username.api.response.GetNameResponse
import org.p2p.wallet.auth.username.api.response.LookupNameResponse
import org.p2p.wallet.auth.username.api.response.ResolveNameResponse
import retrofit2.http.Body
import retrofit2.http.POST
import org.p2p.wallet.auth.username.api.request.RegisterUsernameServiceRequest as Request
import org.p2p.wallet.auth.username.api.response.RegisterUsernameServiceListResponse as ListResponse
import org.p2p.wallet.auth.username.api.response.RegisterUsernameServiceResponse as Response

interface RegisterUsernameServiceApi {

    @POST("./")
    suspend fun getUsername(@Body request: Request<GetNameRequest>):
        Response<GetNameResponse>

    @POST("./")
    suspend fun createUsername(@Body request: Request<CreateNameRequest>):
        Response<CreateNameResponse>

    @POST("./")
    suspend fun resolveUsername(@Body request: Request<ResolveNameRequest>):
        ListResponse<ResolveNameResponse>

    @POST("./")
    suspend fun lookupUsername(@Body request: Request<LookupNameRequest>):
        ListResponse<LookupNameResponse>
}
