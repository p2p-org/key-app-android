package org.p2p.wallet.auth.username.api.request

import com.google.gson.annotations.SerializedName
import org.p2p.core.crypto.Base58String

data class CreateNameRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("owner")
    val owner: Base58String,
    @SerializedName("credentials")
    val credentials: CreateNameRequestCredentials
)

sealed interface CreateNameRequestCredentials {
    class Web3AuthCredentials(
        @SerializedName("timestamp")
        val timestamp: String,
        @SerializedName("signature")
        val signature: Base58String
    ) : CreateNameRequestCredentials

    // do GeeTest later some day
}
