package org.p2p.wallet.striga.wallet.api.request

import com.google.gson.annotations.SerializedName
import org.p2p.core.utils.MillisSinceEpoch

data class StrigaAccountStatementRequest(
    @SerializedName("userId")
    val userId: String,
    @SerializedName("accountId")
    val accountId: String,
    @SerializedName("startDate")
    val startDate: MillisSinceEpoch,
    @SerializedName("endDate")
    val endDate: MillisSinceEpoch,
    @SerializedName("page")
    val page: Int = 1,
)
