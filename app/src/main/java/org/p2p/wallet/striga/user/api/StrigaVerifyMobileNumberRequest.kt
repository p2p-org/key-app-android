package org.p2p.wallet.striga.user.api

import com.google.gson.annotations.SerializedName

class StrigaVerifyMobileNumberRequest(
    @SerializedName("userId")
    val userId: String,
    @SerializedName("verificationCode")
    val verificationCode: String
)
