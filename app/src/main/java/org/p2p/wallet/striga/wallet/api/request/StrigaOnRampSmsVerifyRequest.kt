package org.p2p.wallet.striga.wallet.api.request

import com.google.gson.annotations.SerializedName

class StrigaOnRampSmsVerifyRequest(
    @SerializedName("userId")
    val userId: String,
    @SerializedName("challengeId")
    val challengeId: String,
    @SerializedName("verificationCode")
    val verificationCode: String,
    @SerializedName("ip")
    val ipAddress: String,
)
