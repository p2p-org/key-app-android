package org.p2p.wallet.claim.api

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url
import java.net.URI
import org.p2p.solanaj.model.types.RpcMapRequest
import org.p2p.wallet.claim.api.response.BridgeBundleResponse
import org.p2p.wallet.infrastructure.network.data.CommonResponse

interface BridgeApi {

    @POST
    suspend fun getEthereumBundle(
        @Body request: RpcMapRequest,
        @Url uri: URI
    ): CommonResponse<BridgeBundleResponse>
}
