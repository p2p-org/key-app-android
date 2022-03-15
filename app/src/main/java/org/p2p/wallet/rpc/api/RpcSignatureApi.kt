package org.p2p.wallet.rpc.api

import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.solanaj.model.types.SignatureInformation
import org.p2p.wallet.infrastructure.network.data.CommonResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface RpcSignatureApi {

    @POST
    suspend fun getConfirmedSignaturesForAddress2(
        @Body rpcRequest: RpcRequest,
        @Url url: String = ""
    ): CommonResponse<List<SignatureInformation>>
}