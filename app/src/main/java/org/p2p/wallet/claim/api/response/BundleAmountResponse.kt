package org.p2p.wallet.claim.api.response

import com.google.gson.annotations.SerializedName

class BundleAmountResponse(
    @SerializedName("amount")
    val amount: String?,
    @SerializedName("usd_amount")
    val usdAmount: String?
)
