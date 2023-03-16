package org.p2p.wallet.bridge.model

import com.google.gson.annotations.SerializedName

data class BridgeBundleFees(
    @SerializedName("gas")
    val gasEth: BridgeBundleFee,
    @SerializedName("arbiter")
    val arbiterFee: BridgeBundleFee,
    var createAccount: BridgeBundleFee?,
)
