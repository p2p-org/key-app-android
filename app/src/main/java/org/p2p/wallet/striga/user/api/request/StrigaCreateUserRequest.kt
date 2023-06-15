package org.p2p.wallet.striga.user.api.request

import com.google.gson.annotations.SerializedName

/**
 * @param placeOfBirth ISO31661 Alpha3 - 3 symbols code
 */
open class StrigaCreateUserRequest(
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String,
    @SerializedName("email")
    val userEmail: String,
    @SerializedName("mobile")
    val mobilePhoneDetails: MobileRequest,
    @SerializedName("dateOfBirth")
    val dateOfBirth: DateOfBirthRequest,
    @SerializedName("address")
    val address: AddressRequest,
    @SerializedName("occupation")
    val occupation: String,
    @SerializedName("sourceOfFunds")
    val sourceOfFunds: String,
    @SerializedName("placeOfBirth")
    val placeOfBirth: String, // ISO31661Alpha3

    @SerializedName("expectedIncomingTxVolumeYearly")
    val expectedIncomingTxVolumeYearly: String,
    @SerializedName("expectedOutgoingTxVolumeYearly")
    val expectedOutgoingTxVolumeYearly: String,
    @SerializedName("selfPepDeclaration")
    val isSelfPepDeclaration: Boolean,
    @SerializedName("purposeOfAccount")
    val purposeOfAccount: String,
) {
    /**
     * @param countryCode The country code of the user mobile number (e.g. +44). Must contain a '+'
     * @param number The mobile number of the user - excluding country code. Up to 12 digits in length
     */
    class MobileRequest(
        @SerializedName("countryCode")
        val countryCode: String,
        @SerializedName("number")
        val number: String
    )

    class DateOfBirthRequest(
        @SerializedName("month")
        val month: Int,
        @SerializedName("day")
        val day: Int,
        @SerializedName("year")
        val year: Int
    )

    /**
     * @param country ISO31661 Alpha2 - 2 symbols code
     */
    class AddressRequest(
        @SerializedName("addressLine1")
        val addressLine1: String,
        @SerializedName("addressLine2")
        val addressLine2: String? = null,
        @SerializedName("city")
        val city: String,
        @SerializedName("state")
        val state: String? = null,
        @SerializedName("country")
        val country: String,
        @SerializedName("postalCode")
        val postalCode: String
    )
}
