package org.p2p.wallet.send.model.send_service

import com.google.gson.annotations.SerializedName

enum class SendRentPayerMode {
    @SerializedName("Service")
    Service,
    @SerializedName("UserSol")
    UserSol,
    @SerializedName("UserSameToken")
    UserSameToken,
    Custom
}
