package org.p2p.wallet.auth.gateway.api.request

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class RegisterWalletRequest(
    @SerializedName("params")
    val params: Params
) {
    class Params(
        /**
         * Solana pubkey base58 encoded
         */
        @SerializedName("client_id")
        val clientPublicKeyB58: String,
        @SerializedName("ethereum_id")
        val etheriumPublicKeyB58: String,
        /**
         * no “+”, E.164 format
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

    enum class OtpMethod(val backendName: String) {
        @SerializedName("sms")
        SMS("sms"),

        @SerializedName("call")
        CALL("call")
    }

    @SerializedName("methodName")
    val methodName: String = "register_wallet"

    @SerializedName("jsonrpc")
    val jsonRpcVersion: String = "2.0"

    @SerializedName("id")
    val requestId: String = UUID.randomUUID().toString()
}
