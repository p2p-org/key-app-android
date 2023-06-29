package org.p2p.wallet.history.api

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Url
import org.p2p.solanaj.model.types.RpcMapRequest
import org.p2p.wallet.history.api.model.RpcHistoryResponse
import org.p2p.core.network.data.CommonResponse
import org.p2p.wallet.utils.emptyString

private const val HEADER_CHANNEL_ID = "android"

interface RpcHistoryServiceApi {

    @POST
    @Headers("CHANNEL_ID: $HEADER_CHANNEL_ID")
    suspend fun getTransactionHistory(
        @Body request: RpcMapRequest,
        @Url url: String = emptyString()
    ): CommonResponse<RpcHistoryResponse>
}
