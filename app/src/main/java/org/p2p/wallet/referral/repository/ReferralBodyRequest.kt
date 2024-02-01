package org.p2p.wallet.referral.repository

import com.google.gson.annotations.SerializedName

class ReferralBodyRequest(
    @SerializedName("user")
    val userPublicKey: String,
    @SerializedName("referent")
    val referent: String?,
    @SerializedName("timed_signature")
    val timedSignature: Map<String, Any>
)
