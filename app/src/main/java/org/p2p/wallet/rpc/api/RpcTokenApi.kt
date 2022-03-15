package org.p2p.wallet.rpc.api

import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.solanaj.model.types.TokenAccountBalance
import org.p2p.solanaj.model.types.TokenSupply
import org.p2p.wallet.infrastructure.network.data.CommonResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface RpcTokenApi {

    @POST
    suspend fun getTokenAccountBalance(
        @Body rpcRequest: RpcRequest,
        @Url url: String = ""
    ): CommonResponse<TokenAccountBalance>

    @POST
    suspend fun getTokenAccountBalances(
        @Body rpcRequest: List<RpcRequest>,
        @Url url: String = ""
    ): List<CommonResponse<TokenAccountBalance>>

    @POST
    suspend fun getTokenSupply(
        @Body rpcRequest: RpcRequest,
        @Url url: String = ""
    ): CommonResponse<TokenSupply>
}