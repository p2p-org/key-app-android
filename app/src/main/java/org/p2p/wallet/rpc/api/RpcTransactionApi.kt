package org.p2p.wallet.rpc.api

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url
import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.solanaj.model.types.SimulateTransactionResponse
import org.p2p.core.network.data.CommonResponse
import org.p2p.wallet.utils.emptyString

interface RpcTransactionApi {

    @POST
    suspend fun sendTransaction(
        @Body rpcRequest: RpcRequest,
        @Url url: String = emptyString()
    ): CommonResponse<String>

    @POST
    suspend fun simulateTransaction(
        @Body rpcRequest: RpcRequest,
        @Url url: String = emptyString()
    ): CommonResponse<SimulateTransactionResponse>
}
