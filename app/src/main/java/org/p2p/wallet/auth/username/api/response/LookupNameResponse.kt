package org.p2p.wallet.auth.username.api.response

import com.google.gson.annotations.SerializedName
import org.p2p.wallet.utils.Base58String

class LookupNameResponse(
    @SerializedName("name")
    val usernameWithDomain: String,
    @SerializedName("address")
    val domainAccountAddress: Base58String,
    @SerializedName("parent")
    val parentDomainAddress: Base58String,
    @SerializedName("class")
    val domainClassAddress: Base58String,
    @SerializedName("updated_at")
    val updateAtCacheTime: String
)
