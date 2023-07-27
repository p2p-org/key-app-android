package org.p2p.wallet.striga.wallet.api.response

import com.google.gson.annotations.SerializedName

class StrigaInitEurOffRampResponse(
    @SerializedName("challengeId")
    val challengeId: String,
    @SerializedName("dateExpires")
    val dateExpires: String,
)
