package org.p2p.solanaj.model.types

import com.google.gson.annotations.SerializedName

data class SignatureInformation constructor(
    @SerializedName("memo")
    val memo: Any?,

    @SerializedName("err")
    val err: Any?,

    @SerializedName("signature")
    val signature: String,

    @SerializedName("slot")
    val slot: Long?
) {

    constructor(info: AbstractMap<String, Any?>) : this(
        info["err"],
        info["memo"],
        info["signature"] as String,
        info["slot"] as Long
    )
}
