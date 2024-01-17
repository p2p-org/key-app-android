package org.p2p.wallet.send.model.send_service

import com.google.gson.annotations.SerializedName

enum class NetworkFeeSource {
    @SerializedName("ServiceCoverage")
    ServiceCoverage,
    @SerializedName("UserCompensated")
    UserCompensated,
    @SerializedName("User")
    User
}
