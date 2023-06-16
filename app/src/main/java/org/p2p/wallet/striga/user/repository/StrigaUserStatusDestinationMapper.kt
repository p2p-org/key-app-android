package org.p2p.wallet.striga.user.repository

import timber.log.Timber
import org.p2p.wallet.kyc.model.StrigaKycStatusBanner
import org.p2p.wallet.striga.user.model.StrigaUserStatus
import org.p2p.wallet.striga.user.model.StrigaUserStatusDestination
import org.p2p.wallet.striga.user.model.StrigaUserVerificationStatus

class StrigaUserStatusDestinationMapper {

    fun mapToDestination(userStatus: StrigaUserStatus?, isUserCreated: Boolean): StrigaUserStatusDestination {
        return mapToSignUpStatus(userStatus, isUserCreated)
    }

    fun mapToStatusBanner(userStatus: StrigaUserStatus?): StrigaKycStatusBanner? {
        return when (userStatus?.kysStatus) {
            StrigaUserVerificationStatus.NOT_STARTED,
            StrigaUserVerificationStatus.INITIATED -> StrigaKycStatusBanner.IDENTIFY
            StrigaUserVerificationStatus.PENDING_REVIEW,
            StrigaUserVerificationStatus.ON_HOLD -> StrigaKycStatusBanner.PENDING
            StrigaUserVerificationStatus.APPROVED -> StrigaKycStatusBanner.VERIFICATION_DONE
            StrigaUserVerificationStatus.REJECTED -> StrigaKycStatusBanner.ACTION_REQUIRED
            StrigaUserVerificationStatus.REJECTED_FINAL -> StrigaKycStatusBanner.REJECTED
            else -> null
        }
    }

    private fun isMobileVerified(userDetails: StrigaUserStatus?) =
        userDetails?.isMobileVerified ?: false

    private fun mapToSignUpStatus(status: StrigaUserStatus?, isUserCreated: Boolean): StrigaUserStatusDestination {
        return when {
            !isUserCreated -> {
                StrigaUserStatusDestination.ONBOARDING
            }
            !isMobileVerified(status) -> {
                StrigaUserStatusDestination.SMS_VERIFICATION
            }
            status?.kysStatus != null && status.kysStatus.ordinal > StrigaUserVerificationStatus.UNKNOWN.ordinal -> {
                StrigaUserStatusDestination.SUM_SUB_VERIFICATION
            }
            else -> {
                Timber.d("User status is not defined: cannot navigate to somewhere")
                StrigaUserStatusDestination.NONE
            }
        }
    }
}
