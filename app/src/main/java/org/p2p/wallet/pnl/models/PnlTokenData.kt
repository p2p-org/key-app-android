package org.p2p.wallet.pnl.models

import com.google.gson.annotations.SerializedName

data class PnlTokenData(
    @SerializedName("usd_amount")
    val usdAmount: String,
    @SerializedName("percent")
    val percent: String
) {
    val isNegative: Boolean
        get() = percent.startsWith("-")
}
