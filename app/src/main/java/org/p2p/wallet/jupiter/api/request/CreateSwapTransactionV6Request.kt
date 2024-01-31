package org.p2p.wallet.jupiter.api.request

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import org.p2p.core.crypto.Base58String

data class CreateSwapTransactionV6Request(
    @SerializedName("quoteResponse")
    val route: JsonObject,
    @SerializedName("userPublicKey")
    val userPublicKey: Base58String
)
