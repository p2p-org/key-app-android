package org.p2p.wallet.bridge.api.response

import com.google.gson.annotations.SerializedName

class BridgeBundleFeesResponse(
    @SerializedName("gas")
    val gasFee: BridgeAmountResponse? = null,
    @SerializedName("arbiter")
    val arbiterFee: BridgeAmountResponse? = null,
    @SerializedName("create_account")
    val createAccountFee: BridgeAmountResponse? = null
)
