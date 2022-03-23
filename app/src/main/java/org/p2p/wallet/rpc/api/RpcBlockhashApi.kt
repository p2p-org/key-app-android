package org.p2p.wallet.rpc.api

import org.p2p.solanaj.model.types.RecentBlockhash
import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.wallet.infrastructure.network.data.CommonResponse
import org.p2p.wallet.utils.emptyString
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface RpcBlockhashApi {

    @POST
    suspend fun getRecentBlockhash(
        @Body rpcRequest: RpcRequest,
        @Url url: String = emptyString()
    ): CommonResponse<RecentBlockhash>
}
