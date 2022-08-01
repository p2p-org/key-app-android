package org.p2p.wallet.auth.gateway.api.request

import com.google.gson.annotations.SerializedName

data class ConfirmRegisterWalletRequest(
    /**
     * Solana pubkey base58 encoded
     */
    @SerializedName("client_id")
    val clientSolanaPublicKeyB58: String,
    @SerializedName("ethereum_id")
    val etheriumPublicKeyB58: String,

    @SerializedName("encrypted_share")
    val encryptedOtpShare: String,

    @SerializedName("encrypted_payload")
    val encryptedPayloadB64: String,

    @SerializedName("phone_confirmation_code")
    val otpConfirmationCode: String,
    @SerializedName("signature")
    val requestSignature: String,
    /**
     * example: "2022-07-18 20:55:08.987283300+03:00"
     */
    @SerializedName("timestamp_device")
    val timestamp: String
)
