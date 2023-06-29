package org.p2p.wallet.auth.username.api.response

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import org.p2p.core.crypto.Base58String

data class ResolveNameResponse(
    @SerializedName("name")
    val usernameWithDomain: String,
    @SerializedName("parent")
    val parentDomainAddress: Base58String,
    @SerializedName("owner")
    val domainOwnerAddress: Base58String,
    @SerializedName("class")
    val domainClassAddress: Base58String,
    @SerializedName("data")
    val additionalData: JsonElement
)
