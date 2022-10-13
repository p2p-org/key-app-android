package org.p2p.wallet.auth.gateway.api.request

import com.google.gson.annotations.SerializedName

enum class OtpMethod(val backendName: String) {
    @SerializedName("sms")
    SMS("sms"),

    @SerializedName("call")
    CALL("call")
}
