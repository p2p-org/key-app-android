package org.p2p.wallet.auth.gateway.api.response

import com.google.gson.annotations.SerializedName

data class RegisterWalletResponse(
    /**
     * Ключ, с помощью которого фронт должен зашифровать шару
     */
    @SerializedName("server_key")
    val serverKeyForEncryptShare: String
)
