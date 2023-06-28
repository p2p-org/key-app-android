package org.p2p.wallet.striga.wallet.api.request

import com.google.gson.annotations.SerializedName

class StrigaEnrichAccountRequest(
    @SerializedName("userId")
    val userId: String,
    @SerializedName("accountId")
    val accountId: String,
)
