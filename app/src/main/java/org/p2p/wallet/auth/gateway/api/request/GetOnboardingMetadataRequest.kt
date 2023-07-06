package org.p2p.wallet.auth.gateway.api.request

import com.google.gson.annotations.SerializedName
import org.p2p.core.crypto.Base58String

class GetOnboardingMetadataRequest(
    @SerializedName("solana_pubkey")
    val userPublicKey: Base58String,

    @SerializedName("ethereum_id")
    val etheriumAddress: String,

    @SerializedName("signature")
    val requestSignature: String,
    /**
     * example: "2022-07-18 20:55:08.987283300+03:00"
     */
    @SerializedName("timestamp_device")
    val timestamp: String
)
