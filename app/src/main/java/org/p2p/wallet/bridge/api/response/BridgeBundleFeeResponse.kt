package org.p2p.wallet.bridge.api.response

import com.google.gson.annotations.SerializedName

class BridgeBundleFeeResponse(
    @SerializedName("amount")
    val amount: String,
    @SerializedName("usd_amount")
    val usdAmount: String,
    @SerializedName("chain")
    val chain: String,
    @SerializedName("token")
    val tokenName: String?
)
