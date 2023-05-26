package org.p2p.wallet.bridge.api.response

import com.google.gson.annotations.SerializedName

data class BridgeSendFeesResponse(
    @SerializedName("network_fee")
    val networkFee: BridgeAmountResponse?,
    @SerializedName("network_fee_in_token")
    val networkFeeInToken: BridgeAmountResponse?,
    @SerializedName("message_account_rent")
    val messageAccountRent: BridgeAmountResponse?,
    @SerializedName("message_account_rent_in_token")
    val messageAccountRentInToken: BridgeAmountResponse?,
    @SerializedName("bridge_fee")
    val bridgeFee: BridgeAmountResponse?,
    @SerializedName("bridge_fee_in_token")
    val bridgeFeeInToken: BridgeAmountResponse?,
    @SerializedName("arbiter_fee")
    val arbiterFee: BridgeAmountResponse?,
    @SerializedName("total_amount")
    val totalAmount: BridgeAmountResponse?,
    @SerializedName("recipient_gets_amount")
    val recipientGetsAmount: BridgeAmountResponse?
)
