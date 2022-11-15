package org.p2p.wallet.auth.gateway.api.response

import com.google.gson.annotations.SerializedName
import org.p2p.solanaj.utils.crypto.Base64String

class GetOnboardingMetadataResponse(
    @SerializedName("metadata")
    val onboardingMetadata: Base64String
)
