package org.p2p.wallet.claim.api.response

import com.google.gson.annotations.SerializedName

class BundleFeeResponse(
    @SerializedName("gasEth")
    val gasEth: String? = null,
    @SerializedName("gasUsdAmount")
    val gasUsdAmount: String? = null,
    @SerializedName("arbiterFee")
    val arbiterFee: String? = null,
    @SerializedName("arbiterFeeUsd")
    val arbiterFeeUsd: String? = null
)
