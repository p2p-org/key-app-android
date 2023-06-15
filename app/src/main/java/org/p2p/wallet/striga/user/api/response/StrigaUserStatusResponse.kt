package org.p2p.wallet.striga.user.api.response

import com.google.gson.annotations.SerializedName

data class StrigaUserStatusResponse(
    @SerializedName("userId")
    val userId: String,
    @SerializedName("emailVerified")
    val isEmailVerified: Boolean,
    @SerializedName("mobileVerified")
    val isMobileVerified: Boolean,
    @SerializedName("status")
    val status: String
)
