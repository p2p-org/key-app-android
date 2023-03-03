package org.p2p.ethereumkit.external.api.response

import com.google.gson.annotations.SerializedName
import java.math.BigInteger
import org.p2p.ethereumkit.internal.models.EthAddress

data class TokenBalancesResponse(
    @SerializedName("address")
    val address: EthAddress? = null,
    @SerializedName("tokenBalances")
    val balances: List<TokenBalanceResponse>
)

data class TokenBalanceResponse(
    @SerializedName("contractAddress")
    val contractAddress: EthAddress,
    @SerializedName("tokenBalance")
    val tokenBalance: BigInteger
)

data class TokenMetadataResponse(
    @SerializedName("decimals")
    val decimals: Int,
    @SerializedName("logo")
    val logoUrl: String? = null,
    @SerializedName("name")
    val tokenName: String? = null,
    @SerializedName("symbol")
    val symbol: String? = null
)
