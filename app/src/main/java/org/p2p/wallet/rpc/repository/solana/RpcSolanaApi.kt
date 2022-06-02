package org.p2p.wallet.rpc.repository.solana

import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseQueryBlockState
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseQueryConfig
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseQueryTxMint
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseSubmitTxMint
import org.p2p.solanaj.model.types.AccountInfo
import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.solanaj.model.types.RpcRequest2
import org.p2p.wallet.infrastructure.network.data.CommonResponse
import org.p2p.wallet.utils.emptyString
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface RpcSolanaApi {

    @POST
    suspend fun sendTransaction(
        @Body rpcRequest: RpcRequest,
        @Url url: String = emptyString()
    ): CommonResponse<String>

    @POST
    suspend fun getConfirmedSignatureForAddress(
        @Body rpcRequest: RpcRequest,
        @Url url: String = emptyString()
    ): CommonResponse<List<AbstractMap<String, Any?>>>

    @POST
    suspend fun queryMint(
        @Body rpcRequest: RpcRequest2,
        @Url url: String = emptyString()
    ): CommonResponse<ResponseQueryTxMint>

    @POST
    suspend fun queryBlockState(
        @Body rpcRequest: RpcRequest2,
        @Url url: String = emptyString()
    ): CommonResponse<ResponseQueryBlockState>

    @POST
    suspend fun queryConfig(
        @Body rpcReuest: RpcRequest2,
        @Url url: String = emptyString()
    ): CommonResponse<ResponseQueryConfig>

    @POST
    suspend fun submitTx(
        @Body rpcRequest: RpcRequest2,
        @Url url: String = emptyString()
    ): CommonResponse<ResponseSubmitTxMint>

    @POST
    suspend fun getAccountInfo(
        @Body rpcRequest: RpcRequest,
        @Url url: String = emptyString()
    ): CommonResponse<AccountInfo>
}
