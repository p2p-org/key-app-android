package org.p2p.wallet.history.api.model

import com.google.gson.annotations.SerializedName

data class RpcHistoryTokenResponse(
    @SerializedName("symbol")
    val symbol: String? = null,
    @SerializedName("mint")
    val mint: String,
    @SerializedName("logo_url")
    val logoUrl: String? = null,
    @SerializedName("usd_rate")
    val usdRate: String?,
    @SerializedName("decimals")
    val decimals: Int,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("name")
    val coinGeckoId: String? = null
)
