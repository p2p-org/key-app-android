package org.p2p.wallet.striga.wallet.api.request

import com.google.gson.annotations.SerializedName

class StrigaOnRampSmsResendRequest(
    @SerializedName("userId")
    val userId: String,
    @SerializedName("challengeId")
    val challengeId: String,
)
