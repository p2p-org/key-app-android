package org.p2p.wallet.striga.user.api.response

import com.google.gson.annotations.SerializedName
import org.p2p.core.utils.MillisSinceEpoch

class StrigaUserDetailsResponse(
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String,
    @SerializedName("email")
    val userEmail: String,
    @SerializedName("documentIssuingCountry")
    val documentIssuingCountry: String,
    @SerializedName("nationality")
    val nationality: String,
    @SerializedName("mobile")
    val mobilePhoneDetails: MobileResponse,
    @SerializedName("dateOfBirth")
    val dateOfBirth: DateOfBirthResponse,
    @SerializedName("address")
    val address: AddressResponse,
    @SerializedName("occupation")
    val occupation: String,
    @SerializedName("sourceOfFunds")
    val sourceOfFunds: String,
    @SerializedName("purposeOfAccount")
    val purposeOfAccount: String,
    @SerializedName("selfPepDeclaration")
    val isSelfPepDeclaration: Boolean,
    @SerializedName("placeOfBirth")
    val placeOfBirth: String, // ISO31661Alpha3
    @SerializedName("expectedIncomingTxVolumeYearly")
    val expectedIncomingTxVolumeYearly: String,
    @SerializedName("expectedOutgoingTxVolumeYearly")
    val expectedOutgoingTxVolumeYearly: String,
    @SerializedName("KYC")
    val kycDetails: KycDataResponse,
    @SerializedName("userId")
    val userId: String,
    @SerializedName("createdAt")
    val createdAt: MillisSinceEpoch
) {
    class MobileResponse(
        @SerializedName("countryCode")
        val countryCode: String,
        @SerializedName("number")
        val number: String
    )

    class DateOfBirthResponse(
        @SerializedName("month")
        val month: String,
        @SerializedName("day")
        val day: String,
        @SerializedName("year")
        val year: String
    )

    class AddressResponse(
        @SerializedName("addressLine1")
        val addressLine1: String,
        @SerializedName("addressLine2")
        val addressLine2: String?,
        @SerializedName("city")
        val city: String,
        @SerializedName("state")
        val state: String?,
        @SerializedName("country")
        val country: String,
        @SerializedName("postalCode")
        val postalCode: String
    )

    class KycDataResponse(
        @SerializedName("emailVerified")
        val isEmailVerified: Boolean,
        @SerializedName("mobileVerified")
        val isMobileVerified: Boolean,
        @SerializedName("status")
        val status: String,
        @SerializedName("details")
        val details: List<String>? = emptyList(),
        @SerializedName("rejectionComments")
        val rejectionComments: RejectionCommentsResponse?
    ) {
        class RejectionCommentsResponse(
            @SerializedName("userComment")
            val userComment: String,
            @SerializedName("autoComment")
            val autoComment: String
        )
    }
}
