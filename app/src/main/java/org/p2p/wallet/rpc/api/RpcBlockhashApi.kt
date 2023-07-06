package org.p2p.wallet.rpc.api

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url
import org.p2p.solanaj.model.types.RecentBlockhashResponse
import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.core.network.data.CommonResponse
import org.p2p.wallet.utils.emptyString

interface RpcBlockhashApi {

    @POST
    suspend fun getRecentBlockhash(
        @Body rpcRequest: RpcRequest,
        @Url url: String = emptyString()
    ): CommonResponse<RecentBlockhashResponse>
}
