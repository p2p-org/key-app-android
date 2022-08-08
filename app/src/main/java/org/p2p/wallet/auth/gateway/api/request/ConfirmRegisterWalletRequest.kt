package org.p2p.wallet.auth.gateway.api.request

import com.google.gson.annotations.SerializedName

data class ConfirmRegisterWalletRequest(
    @SerializedName("client_id")
    val clientSolanaPublicKey: String,
    @SerializedName("ethereum_id")
    val etheriumPublicKey: String,

    @SerializedName("encrypted_share")
    val thirdShare: String,

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
