package org.p2p.wallet.rpc.api

import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.solanaj.model.types.RpcResultTypes
import org.p2p.solanaj.model.types.TokenAccountBalance
import org.p2p.solanaj.model.types.TokenSupply
import org.p2p.core.network.data.CommonResponse
import org.p2p.wallet.utils.emptyString
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface RpcBalanceApi {

    @POST
    suspend fun getBalance(
        @Body rpcRequest: RpcRequest,
        @Url url: String = emptyString()
    ): CommonResponse<RpcResultTypes.ValueLong>

    @POST
    suspend fun getBalances(
        @Body rpcRequest: List<RpcRequest>,
        @Url url: String = emptyString()
    ): List<CommonResponse<RpcResultTypes.ValueLong>>

    @POST
    suspend fun getTokenAccountBalance(
        @Body rpcRequest: RpcRequest,
        @Url url: String = emptyString()
    ): CommonResponse<TokenAccountBalance>

    @POST
    suspend fun getTokenAccountBalances(
        @Body rpcRequest: List<RpcRequest>,
        @Url url: String = emptyString()
    ): List<CommonResponse<TokenAccountBalance>>

    @POST
    suspend fun getTokenSupply(
        @Body rpcRequest: RpcRequest,
        @Url url: String = emptyString()
    ): CommonResponse<TokenSupply>
}
