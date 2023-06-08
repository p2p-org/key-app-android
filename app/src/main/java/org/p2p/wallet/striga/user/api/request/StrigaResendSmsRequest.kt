package org.p2p.wallet.striga.user.api.request

import com.google.gson.annotations.SerializedName

class StrigaResendSmsRequest(
    @SerializedName("userId")
    val userId: String,
)
