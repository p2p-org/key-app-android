package org.p2p.solanaj.model.types

import com.google.gson.annotations.SerializedName

data class SignatureInformationResponse constructor(
    @SerializedName("memo")
    val memo: Any?,

    @SerializedName("err")
    val transactionFailure: Any?,

    @SerializedName("signature")
    val signature: String,

    @SerializedName("slot")
    val slot: Long?,

    @SerializedName("confirmationStatus")
    val confirmationStatus: ConfirmationStatus
) {

    constructor(info: AbstractMap<String, Any?>) : this(
        info["err"],
        info["memo"],
        info["signature"] as String,
        info["slot"] as Long,
        info["confirmationStatus"] as ConfirmationStatus
    )
}
