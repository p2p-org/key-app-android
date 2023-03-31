package org.p2p.wallet.bridge.api.response

import com.google.gson.annotations.SerializedName

data class BridgeSendFeesResponse(
    @SerializedName("network_fee")
    val networkFee: BridgeAmountResponse?,
    @SerializedName("message_account_rent")
    val messageAccountRent: BridgeAmountResponse?,
    @SerializedName("bridge_fee")
    val bridgeFee: BridgeAmountResponse?,
    @SerializedName("arbiter_fee")
    val arbiterFee: BridgeAmountResponse?
)
