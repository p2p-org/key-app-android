package org.p2p.wallet.bridge.api.response

import com.google.gson.annotations.SerializedName

class BridgeBundleFeesResponse(
    @SerializedName("gas")
    val gasFee: BridgeBundleFeeResponse? = null,
    @SerializedName("arbiter")
    val arbiterFee: BridgeBundleFeeResponse? = null,
    @SerializedName("create_account")
    val createAccountFee: BridgeBundleFeeResponse? = null
)
