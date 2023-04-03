package org.p2p.wallet.bridge.api.response

import com.google.gson.annotations.SerializedName

class BridgeAmountResponse(
    @SerializedName("amount")
    val amount: String,
    @SerializedName("usd_amount")
    val usdAmount: String,
    @SerializedName("chain")
    val chain: String?,
    @SerializedName("token")
    val tokenName: String?,
    @SerializedName("decimals")
    val tokenDecimals: Int,
    @SerializedName("symbol")
    val tokenSymbol: String,
)
