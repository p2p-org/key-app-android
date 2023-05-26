package org.p2p.wallet.striga.user.repository

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import org.p2p.wallet.striga.model.StrigaDataLayerError
import org.p2p.wallet.striga.user.api.response.StrigaUserDetailsResponse
import org.p2p.wallet.striga.user.model.StrigaUserAddress
import org.p2p.wallet.striga.user.model.StrigaUserDetails
import org.p2p.wallet.striga.user.model.StrigaUserInfo
import org.p2p.wallet.striga.user.model.StrigaUserKycInfo
import org.p2p.wallet.striga.user.model.StrigaUserVerificationStatus

class StrigaUserRepositoryMapper {
    @Throws(StrigaDataLayerError.MappingFailed::class)
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
            throw StrigaDataLayerError.MappingFailed(mappingFailed.message.orEmpty())
        }
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
}
