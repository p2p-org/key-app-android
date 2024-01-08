package org.p2p.token.service.api.response

import com.google.gson.annotations.SerializedName

internal data class TokenItemMetadataResponse(
    @SerializedName("address")
    val address: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("symbol")
    val symbol: String,
    @SerializedName("logoURI")
    val logoUrl: String?,
    @SerializedName("decimals")
    val decimals: Int,
    @SerializedName("chainId")
    val chainId: Int
)
