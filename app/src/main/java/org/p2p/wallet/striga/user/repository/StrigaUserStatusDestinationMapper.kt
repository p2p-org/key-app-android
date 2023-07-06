package org.p2p.wallet.striga.user.repository

import timber.log.Timber
import org.p2p.wallet.kyc.model.StrigaKycStatusBanner
import org.p2p.wallet.striga.user.model.StrigaUserStatusDestination
import org.p2p.wallet.striga.user.model.StrigaUserStatusDetails
import org.p2p.wallet.striga.user.model.StrigaUserVerificationStatus

class StrigaUserStatusDestinationMapper {

    fun mapToDestination(userStatus: StrigaUserStatusDetails?): StrigaUserStatusDestination {
        return when {
            // if user is not created
            userStatus == null -> {
                StrigaUserStatusDestination.ONBOARDING
            }
            !userStatus.isMobileVerified -> {
                StrigaUserStatusDestination.SMS_VERIFICATION
            }
            userStatus.isKycOpenable -> {
                StrigaUserStatusDestination.SUM_SUB_VERIFICATION
            }
            userStatus.isKycPending -> {
                StrigaUserStatusDestination.KYC_PENDING
            }
            userStatus.isKycApproved -> {
                StrigaUserStatusDestination.IBAN_ACCOUNT
            }
            else -> {
                Timber.d("User status is not defined: cannot navigate to somewhere")
                StrigaUserStatusDestination.NONE
            }
        }
    }

    fun mapToStatusBanner(userStatus: StrigaUserStatusDetails?): StrigaKycStatusBanner? {
        if (userStatus == null || !userStatus.isMobileVerified) return null
        return when (userStatus.kycStatus) {
            StrigaUserVerificationStatus.NOT_STARTED,
            StrigaUserVerificationStatus.INITIATED -> StrigaKycStatusBanner.IDENTIFY
            StrigaUserVerificationStatus.PENDING_REVIEW,
            StrigaUserVerificationStatus.ON_HOLD -> StrigaKycStatusBanner.PENDING
            StrigaUserVerificationStatus.APPROVED -> StrigaKycStatusBanner.VERIFICATION_DONE
            StrigaUserVerificationStatus.REJECTED -> StrigaKycStatusBanner.ACTION_REQUIRED
            StrigaUserVerificationStatus.REJECTED_FINAL -> StrigaKycStatusBanner.REJECTED
            StrigaUserVerificationStatus.UNKNOWN -> null
        }
    }
}
