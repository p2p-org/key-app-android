package org.p2p.wallet.rpc.api

import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.solanaj.model.types.RpcResultTypes
import org.p2p.wallet.infrastructure.network.data.CommonResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface RpcBalanceApi {

    @POST
    suspend fun getBalance(
        @Body rpcRequest: RpcRequest,
        @Url url: String = ""
    ): CommonResponse<RpcResultTypes.ValueLong>

    @POST
    suspend fun getBalances(
        @Body rpcRequest: List<RpcRequest>,
        @Url url: String = ""
    ): List<CommonResponse<RpcResultTypes.ValueLong>>

    @POST
    suspend fun getMinimumBalanceForRentExemption(
        @Body rpcRequest: RpcRequest,
        @Url url: String = ""
    ): CommonResponse<Long>
}