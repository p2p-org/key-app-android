package org.p2p.wallet.rpc.repository.ren

import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseQueryBlockState
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseQueryConfig
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseQueryTxMint
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseSubmitTxMint
import org.p2p.solanaj.model.types.RpcMapRequest
import org.p2p.core.network.data.CommonResponse
import org.p2p.wallet.utils.emptyString
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface RenPoolApi {

    @POST
    suspend fun queryMint(
        @Body rpcRequest: RpcMapRequest,
        @Url url: String = emptyString()
    ): CommonResponse<ResponseQueryTxMint>

    @POST
    suspend fun queryBlockState(
        @Body rpcRequest: RpcMapRequest,
        @Url url: String = emptyString()
    ): CommonResponse<ResponseQueryBlockState>

    @POST
    suspend fun queryConfig(
        @Body rpcReuest: RpcMapRequest,
        @Url url: String = emptyString()
    ): CommonResponse<ResponseQueryConfig>

    @POST
    suspend fun submitTx(
        @Body rpcRequest: RpcMapRequest,
        @Url url: String = emptyString()
    ): CommonResponse<ResponseSubmitTxMint>
}
