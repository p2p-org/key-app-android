package org.p2p.wallet.auth.gateway.api.request

import com.google.gson.annotations.SerializedName
import org.p2p.core.crypto.Base58String

data class RegisterWalletRequest(
    /**
     * Solana pubkey base58 encoded
     */
    @SerializedName("solana_pubkey")
    val clientSolanaPublicKey: Base58String,
    @SerializedName("ethereum_id")
    val etheriumAddress: String,
    /**
     * E.164 format
     */
    @SerializedName("phone")
    val userPhone: String,
    @SerializedName("app_hash")
    val appHash: String,
    @SerializedName("channel")
    val channel: OtpMethod,
    @SerializedName("signature")
    val requestSignature: String,
    /**
     * example: "2022-07-18 20:55:08.987283300+03:00"
     */
    @SerializedName("timestamp_device")
    val timestamp: String
)
