package org.p2p.solanaj.model.types

import com.google.gson.annotations.SerializedName

enum class Encoding(
    @SerializedName("enc")
    val encoding: String
) {
    @SerializedName("base64")
    BASE64("base64"),

    @Deprecated("Slow for solana, use Base 58")
    @SerializedName("base58")
    BASE58("base58");
}
