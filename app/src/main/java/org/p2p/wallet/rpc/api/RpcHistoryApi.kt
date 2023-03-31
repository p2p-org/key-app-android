package org.p2p.wallet.rpc.api

import org.p2p.solanaj.kits.transaction.network.ConfirmedTransactionRootResponse
import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.solanaj.model.types.SimulateTransactionResponse
import org.p2p.wallet.infrastructure.network.data.CommonResponse
import org.p2p.wallet.utils.emptyString
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface RpcHistoryApi {

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

    @POST
    suspend fun getConfirmedTransactions(
        @Body rpcRequest: List<RpcRequest>,
        @Url url: String = emptyString()
    ): List<CommonResponse<ConfirmedTransactionRootResponse>>
}
