package org.p2p.wallet.striga.user.repository

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import org.p2p.wallet.striga.StrigaUserConstants
import org.p2p.wallet.striga.model.StrigaDataLayerError
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupData
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType
import org.p2p.wallet.striga.user.api.request.StrigaCreateUserRequest
import org.p2p.wallet.striga.user.api.response.StrigaCreateUserResponse
import org.p2p.wallet.striga.user.api.response.StrigaUserDetailsResponse
import org.p2p.wallet.striga.user.api.response.StrigaUserStatusResponse
import org.p2p.wallet.striga.user.model.StrigaUserStatus
import org.p2p.wallet.striga.user.model.StrigaUserDetails
import org.p2p.wallet.striga.user.model.StrigaUserVerificationStatus
import org.p2p.wallet.striga.user.model.StrigaUserInfo
import org.p2p.wallet.striga.user.model.StrigaUserKycInfo
import org.p2p.wallet.striga.user.model.StrigaUserAddress
import org.p2p.wallet.striga.user.model.StrigaUserInitialDetails
import org.p2p.wallet.striga.user.model.StrigaUserInitialKycDetails

class StrigaUserRepositoryMapper {
    @Throws(StrigaDataLayerError.InternalError::class)
    fun fromNetwork(response: StrigaUserDetailsResponse): StrigaUserDetails {
        return try {
            StrigaUserDetails(
                userId = response.userId,
                createdAt = response.createdAt,
                userInfo = response.toUserInfo(),
                addressDetails = response.address.toAddressDetails(),
                kycDetails = response.toKycDetails()
            )
        } catch (mappingFailed: Throwable) {
            throw StrigaDataLayerError.InternalError(mappingFailed, "StrigaUserDetails mapping failed")
        }
    }

    fun fromNetwork(response: StrigaUserStatusResponse): StrigaUserStatus {
        return StrigaUserStatus(
            userId = response.userId,
            isMobileVerified = response.isMobileVerified,
            isEmailVerified = response.isEmailVerified,
            kysStatus = StrigaUserVerificationStatus.from(response.status)
        )
    }

    fun fromNetwork(response: StrigaCreateUserResponse): StrigaUserInitialDetails {
        return try {
            StrigaUserInitialDetails(
                userId = response.userId,
                email = response.email,
                kycStatus = response.toKycDetails()
            )
        } catch (mappingFailed: Throwable) {
            throw StrigaDataLayerError.InternalError(mappingFailed, "StrigaUserInitialDetails mapping failed")
        }
    }

    fun toNetwork(signupData: List<StrigaSignupData>): StrigaCreateUserRequest {
        val map = signupData.filter { it.value != null }.associate { it.type to it.value!! }

        val birthday = map.getOrThrow(StrigaSignupDataType.DATE_OF_BIRTH).split(".")
        check(birthday.size == 3) { "Birthday should contains 3 segments of digits separated by a dot" }

        return StrigaCreateUserRequest(
            firstName = map.getOrThrow(StrigaSignupDataType.FIRST_NAME),
            lastName = map.getOrThrow(StrigaSignupDataType.LAST_NAME),
            userEmail = map.getOrThrow(StrigaSignupDataType.EMAIL),
            mobilePhoneDetails = StrigaCreateUserRequest.MobileRequest(
                countryCode = map.getOrThrow(StrigaSignupDataType.PHONE_CODE_WITH_PLUS),
                number = map.getOrThrow(StrigaSignupDataType.PHONE_NUMBER)
            ),
            dateOfBirth = StrigaCreateUserRequest.DateOfBirthRequest(
                day = birthday[0].toInt(),
                month = birthday[1].toInt(),
                year = birthday[2].toInt()
            ),
            address = StrigaCreateUserRequest.AddressRequest(
                addressLine1 = map.getOrThrow(StrigaSignupDataType.CITY_ADDRESS_LINE),
                addressLine2 = map.getOrThrow(StrigaSignupDataType.CITY_ADDRESS_LINE),
                city = map.getOrThrow(StrigaSignupDataType.CITY),
                state = map.getOrThrow(StrigaSignupDataType.CITY_STATE),
                country = map.getOrThrow(StrigaSignupDataType.COUNTRY_ALPHA_2),
                postalCode = map.getOrThrow(StrigaSignupDataType.CITY_POSTAL_CODE)
            ),
            occupation = map.getOrThrow(StrigaSignupDataType.OCCUPATION),
            sourceOfFunds = map.getOrThrow(StrigaSignupDataType.SOURCE_OF_FUNDS),
            placeOfBirth = map.getOrThrow(StrigaSignupDataType.COUNTRY_OF_BIRTH_ALPHA_3),
            expectedIncomingTxVolumeYearly = StrigaUserConstants.EXPECTED_INCOMING_TX_YEARLY,
            expectedOutgoingTxVolumeYearly = StrigaUserConstants.EXPECTED_OUTGOING_TX_YEARLY,
            isSelfPepDeclaration = StrigaUserConstants.SELF_PEP_DECLARATION,
            purposeOfAccount = StrigaUserConstants.PURPOSE_OF_ACCOUNT
        )
    }

    private fun StrigaUserDetailsResponse.toUserInfo(): StrigaUserInfo {
        val dateOfBirthDateTime = ZonedDateTime.of(
            LocalDate.of(dateOfBirth.year.toInt(), dateOfBirth.month.toInt(), dateOfBirth.day.toInt()),
            LocalTime.MIN,
            ZoneId.systemDefault()
        )
        return StrigaUserInfo(
            firstName = firstName,
            lastName = lastName,
            dateOfBirth = dateOfBirthDateTime,
            nationality = nationality,
            email = userEmail,
            phoneNumber = mobilePhoneDetails.number,
            phoneNumberCode = mobilePhoneDetails.countryCode,
            placeOfBirth = placeOfBirth
        )
    }

    private fun StrigaUserDetailsResponse.AddressResponse.toAddressDetails(): StrigaUserAddress {
        return StrigaUserAddress(
            addressLine1 = addressLine1,
            addressLine2 = addressLine2,
            city = city,
            state = state,
            country = country,
            postalCode = postalCode
        )
    }

    private fun StrigaUserDetailsResponse.toKycDetails(): StrigaUserKycInfo {
        return StrigaUserKycInfo(
            occupation = occupation,
            sourceOfFunds = sourceOfFunds,
            purposeOfAccount = purposeOfAccount,
            isEmailVerified = kycDetails.isEmailVerified,
            isMobileVerified = kycDetails.isMobileVerified,
            kycStatus = StrigaUserVerificationStatus.from(kycDetails.status),
            rejectionDetails = kycDetails.details,
            rejectionUserToAutoComments = kycDetails.rejectionComments.run { userComment to autoComment }
        )
    }

    private fun StrigaCreateUserResponse.toKycDetails(): StrigaUserInitialKycDetails {
        val status = StrigaUserVerificationStatus.from(kycDetails.status)
        if (status == StrigaUserVerificationStatus.UNKNOWN) {
            throw StrigaDataLayerError.InternalError(
                IllegalStateException("Unsupported KYC status: ${kycDetails.status}")
            )
        }
        return StrigaUserInitialKycDetails(
            status = status,
        )
    }

    private fun <A, B> Map<A, B>.getOrThrow(key: A): B {
        return this[key] ?: throw StrigaDataLayerError.InternalError(IllegalStateException("Key $key not found in map"))
    }
}
