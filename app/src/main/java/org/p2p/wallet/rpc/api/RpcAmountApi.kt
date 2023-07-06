package org.p2p.wallet.rpc.api

import org.p2p.solanaj.model.types.FeesResponse
import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.core.network.data.CommonResponse
import org.p2p.wallet.utils.emptyString
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface RpcAmountApi {

    @POST
    suspend fun getFees(
        @Body rpcRequest: RpcRequest,
        @Url url: String = emptyString()
    ): CommonResponse<FeesResponse>

    @POST
    suspend fun getMinimumBalanceForRentExemption(
        @Body rpcRequest: RpcRequest,
        @Url url: String = emptyString()
    ): CommonResponse<Long>
}
