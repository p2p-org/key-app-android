package org.p2p.wallet.striga.user.repository

import org.p2p.wallet.striga.user.model.StrigaUserStatus
import org.p2p.wallet.striga.user.model.StrigaUserStatusDestination
import org.p2p.wallet.striga.user.model.StrigaUserVerificationStatus

class StrigaUserStatusDestinationMapper {

    fun mapToDestination(userStatus: StrigaUserStatus?, isUserCreated: Boolean): StrigaUserStatusDestination {
        return mapToSignUpStatus(userStatus, isUserCreated)
    }

    private fun isMobileVerified(userDetails: StrigaUserStatus?) =
        userDetails?.isMobileVerified ?: false

    private fun mapToSignUpStatus(status: StrigaUserStatus?, isUserCreated: Boolean): StrigaUserStatusDestination {

        return when {
            !isUserCreated -> {
                StrigaUserStatusDestination.NONE
            }
            !isMobileVerified(status) -> {
                StrigaUserStatusDestination.SMS_VERIFICATION
            }
            status?.kysStatus == StrigaUserVerificationStatus.INITIATED -> {
                StrigaUserStatusDestination.SMS_SUB_VERIFICATION
            }
            else -> {
                StrigaUserStatusDestination.NONE
            }
        }
    }
}
