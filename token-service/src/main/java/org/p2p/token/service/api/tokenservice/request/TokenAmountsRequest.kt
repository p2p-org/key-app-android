package org.p2p.token.service.api.tokenservice.request

import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import org.p2p.core.rpc.JsonRpc
import org.p2p.token.service.api.tokenservice.response.TokenAmountsResponse

internal data class TokenAmountsRequest(
    @Transient val request: TokenAmountsBodyRequest
) : JsonRpc<TokenAmountsBodyRequest, List<TokenAmountsResponse>>(
    method = "get_token_amount",
    params = request
) {
    @Transient
    override val typeOfResult: Type =
        object : TypeToken<List<TokenAmountsResponse>>() {}.type
}

internal class TokenAmountsBodyRequest(
    @SerializedName("vs_token")
    val vsTokenMint: String,
    @SerializedName("amount")
    val amountLamports: String,
    @SerializedName("mints")
    val mints: List<String>
)
