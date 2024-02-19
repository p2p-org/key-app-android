package org.p2p.ethereumkit.external.api.alchemy.response

import com.google.gson.annotations.SerializedName
import org.p2p.core.wrapper.eth.EthAddress

internal data class TokenBalancesResponse(
    @SerializedName("address")
    val address: EthAddress? = null,
    @SerializedName("tokenBalances")
    val balances: List<TokenBalanceResponse>
)

internal data class TokenBalanceResponse(
    @SerializedName("contractAddress")
    val contractAddress: EthAddress,
    @SerializedName("tokenBalance")
    val tokenBalanceHex: String
)

internal data class TokenMetadataResponse(
    @SerializedName("decimals")
    val decimals: Int,
    @SerializedName("logo")
    val logoUrl: String? = null,
    @SerializedName("name")
    val tokenName: String? = null,
    @SerializedName("symbol")
    val symbol: String? = null
)
