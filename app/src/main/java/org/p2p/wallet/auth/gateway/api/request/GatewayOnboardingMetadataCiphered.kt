package org.p2p.wallet.auth.gateway.api.request

import com.google.gson.annotations.SerializedName
import org.p2p.solanaj.utils.crypto.Base64String

data class GatewayOnboardingMetadataCiphered(
    @SerializedName("nonce")
    val nonce: Base64String,
    @SerializedName("metadata_ciphered")
    val metadataCiphered: Base64String,
    @SerializedName("tag")
    val tag: Base64String
)
