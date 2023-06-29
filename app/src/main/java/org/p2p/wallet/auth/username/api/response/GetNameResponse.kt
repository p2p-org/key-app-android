package org.p2p.wallet.auth.username.api.response

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import org.p2p.core.crypto.Base58String

data class GetNameResponse(
    @SerializedName("parent")
    val parentAddress: Base58String,
    @SerializedName("owner")
    val ownerAddress: Base58String,
    @SerializedName("class")
    val classAddress: Base58String,
    @SerializedName("data")
    val additionalData: JsonElement
)
