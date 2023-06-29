package org.p2p.wallet.auth.username.api.request

import com.google.gson.annotations.SerializedName
import org.p2p.core.crypto.Base58String

class LookupNameRequest(
    @SerializedName("owner")
    val owner: Base58String,
    @SerializedName("with_tld")
    val withTld: Boolean = true
)
