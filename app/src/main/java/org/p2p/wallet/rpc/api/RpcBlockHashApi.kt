package org.p2p.wallet.rpc.api

import org.p2p.solanaj.model.types.RecentBlockhash
import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.wallet.infrastructure.network.data.CommonResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface RpcBlockHashApi {

    @POST
    suspend fun getRecentBlockhash(
        @Body rpcRequest: RpcRequest,
        @Url url: String = ""
    ): CommonResponse<RecentBlockhash>
}