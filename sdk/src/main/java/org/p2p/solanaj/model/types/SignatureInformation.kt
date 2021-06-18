package org.p2p.solanaj.model.types

import com.google.gson.annotations.SerializedName

data class SignatureInformation(
    @SerializedName("blockTime")
    val blockTime: Long,

    @SerializedName("confirmationStatus")
    val confirmationStatus: String,

    @SerializedName("signature")
    val signature: String,

    @SerializedName("slot")
    val slot: Long
)