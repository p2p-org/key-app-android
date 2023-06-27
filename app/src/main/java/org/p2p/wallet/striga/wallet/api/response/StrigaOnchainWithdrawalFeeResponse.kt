package org.p2p.wallet.striga.wallet.api.response

import com.google.gson.annotations.SerializedName

data class StrigaOnchainWithdrawalFeeResponse(
    @SerializedName("totalFee")
    val totalFee: String,
    @SerializedName("networkFee")
    val networkFee: String,
    @SerializedName("ourFee")
    val ourFee: String,
    @SerializedName("theirFee")
    val theirFee: String,
    @SerializedName("feeCurrency")
    val feeCurrency: String,
    @SerializedName("gasLimit")
    val gasLimit: String,
    @SerializedName("gasPrice")
    val gasPrice: String
)
