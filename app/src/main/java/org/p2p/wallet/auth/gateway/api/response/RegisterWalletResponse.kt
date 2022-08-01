package org.p2p.wallet.auth.gateway.api.response

import com.google.gson.annotations.SerializedName

data class RegisterWalletResponse(
    /**
     * Key to encrypt third share
     */
    @SerializedName("server_key")
    val serverKeyForEncryptShare: String
)
