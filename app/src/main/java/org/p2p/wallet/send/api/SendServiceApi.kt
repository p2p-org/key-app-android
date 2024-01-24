package org.p2p.wallet.send.api

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url
import org.p2p.core.crypto.Base58String
import org.p2p.core.network.data.CommonResponse
import org.p2p.solanaj.model.types.RpcMapRequest
import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.wallet.send.api.responses.SendGeneratedTransactionResponse
import org.p2p.wallet.send.api.responses.SendTokenAmountsResponse
import org.p2p.wallet.utils.emptyString

interface SendServiceApi {
    @POST
    suspend fun getCompensationTokens(
        @Body rpcRequest: RpcRequest,
        @Url url: String = emptyString()
    ): CommonResponse<List<Base58String>>

    @POST
    suspend fun generateTransaction(
        @Body rpcRequest: RpcMapRequest,
        @Url url: String = emptyString()
    ): CommonResponse<SendGeneratedTransactionResponse>

    @POST
    suspend fun getTokenAmounts(
        @Body rpcRequest: RpcMapRequest,
        @Url url: String = emptyString()
    ): CommonResponse<List<SendTokenAmountsResponse>>

}
