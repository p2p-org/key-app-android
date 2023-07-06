package org.p2p.wallet.striga.user.api.response

import com.google.gson.annotations.SerializedName

class StrigaCreateUserResponse(
    @SerializedName("userId")
    val userId: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("KYC")
    val kycDetails: KycDataResponse

) {
    class KycDataResponse(
        @SerializedName("status")
        val status: String,
        @SerializedName("emailVerified")
        val isEmailVerified: Boolean,
        @SerializedName("mobileVerified")
        val isMobileVerified: Boolean,
    )
}
