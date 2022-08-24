package org.p2p.wallet.auth.gateway.api.request

import com.google.gson.annotations.SerializedName

data class RestoreWalletRequest(
    @SerializedName("restore_id")
    val temporarySolanaPublicKey: String,

    @SerializedName("phone")
    val userPhone: String,

    @SerializedName("app_hash")
    val appHash: String,

    @SerializedName("channel")
    val channelMethod: OtpMethod,

    @SerializedName("signature")
    val requestSignature: String,
    /**
     * example: "2022-07-18 20:55:08.987283300+03:00"
     */
    @SerializedName("timestamp_device")
    val timestamp: String
)
