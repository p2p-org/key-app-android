package org.p2p.wallet.referral.repository

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url
import org.p2p.core.network.data.CommonResponse
import org.p2p.solanaj.model.types.RpcMapRequest
import org.p2p.wallet.utils.emptyString

interface ReferralApi {
    @POST
    suspend fun setReferent(
        @Body rpcRequest: RpcMapRequest,
        @Url url: String = emptyString()
    ): CommonResponse<String>
}
