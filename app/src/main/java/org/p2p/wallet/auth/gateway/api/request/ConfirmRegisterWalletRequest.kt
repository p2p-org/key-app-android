package org.p2p.wallet.auth.gateway.api.request

import com.google.gson.annotations.SerializedName
import org.p2p.core.crypto.Base64String
import org.p2p.core.crypto.Base58String

data class ConfirmRegisterWalletRequest(
    @SerializedName("solana_pubkey")
    val clientSolanaPublicKey: Base58String,
    @SerializedName("ethereum_id")
    val etheriumAddress: String,

    @SerializedName("encrypted_share")
    val thirdShare: Base64String,

    @SerializedName("encrypted_metadata")
    val onboardingMetadata: Base64String,

    @SerializedName("encrypted_payload")
    val encryptedPayloadB64: Base64String,

    @SerializedName("phone_confirmation_code")
    val otpConfirmationCode: String,
    @SerializedName("signature")
    val requestSignature: Base58String,
    /**
     * example: "2022-07-18 20:55:08.987283300+03:00"
     */
    @SerializedName("timestamp_device")
    val timestamp: String,
    @SerializedName("phone")
    val phone: String
)
