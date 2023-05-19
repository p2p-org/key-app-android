package org.p2p.wallet.rpc.repository.solana

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url
import org.p2p.solanaj.model.types.AccountInfo
import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.solanaj.model.types.SimulateTransactionResponse
import org.p2p.solanaj.rpc.api.RecentPerformanceSampleResponse
import org.p2p.wallet.infrastructure.network.data.CommonResponse
import org.p2p.wallet.utils.emptyString

interface RpcSolanaApi {

    @POST
    suspend fun sendTransaction(
        @Body rpcRequest: RpcRequest,
        @Url url: String = emptyString()
    ): CommonResponse<String> // First Transaction Signature embedded in the transaction

    @POST
    suspend fun simulateTransaction(
        @Body rpcRequest: RpcRequest,
        @Url url: String = emptyString()
    ): CommonResponse<SimulateTransactionResponse>

    @POST
    suspend fun getConfirmedSignatureForAddress(
        @Body rpcRequest: RpcRequest,
        @Url url: String = emptyString()
    ): CommonResponse<List<AbstractMap<String, Any?>>>

    @POST
    suspend fun getRecentPerformanceSamples(
        @Body rpcRequest: RpcRequest,
        @Url url: String = emptyString()
    ): CommonResponse<List<RecentPerformanceSampleResponse>>

    @POST
    suspend fun getAccountInfo(
        @Body rpcRequest: RpcRequest,
        @Url url: String = emptyString()
    ): CommonResponse<AccountInfo>
}
