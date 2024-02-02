package org.p2p.wallet.pnl.api

import com.google.gson.JsonElement
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url
import org.p2p.core.network.data.CommonResponse
import org.p2p.solanaj.model.types.RpcMapRequest
import org.p2p.wallet.utils.emptyString

interface PnlServiceApi {
    @POST
    suspend fun getAccountPnl(
        @Body rpcRequest: RpcMapRequest,
        @Url url: String = emptyString()
    ): CommonResponse<JsonElement>
}
