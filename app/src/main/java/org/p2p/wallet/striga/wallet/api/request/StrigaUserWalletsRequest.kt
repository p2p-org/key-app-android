package org.p2p.wallet.striga.wallet.api.request

import com.google.gson.annotations.SerializedName
import org.p2p.core.utils.MillisSinceEpoch

class StrigaUserWalletsRequest(
    @SerializedName("userId")
    val userId: String,
    @SerializedName("startDate")
    val startDate: MillisSinceEpoch,
    @SerializedName("endDate")
    val endDate: MillisSinceEpoch,
    @SerializedName("page")
    val page: Long
)
