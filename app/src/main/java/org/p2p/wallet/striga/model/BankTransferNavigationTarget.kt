package org.p2p.wallet.striga.model

import org.p2p.wallet.striga.signup.model.StrigaUserStatus
import org.p2p.wallet.striga.user.model.StrigaUserVerificationStatus

enum class BankTransferNavigationTarget {
    Nowhere,
    StrigaOnboarding,
    StrigaSignupFirstStep,
    StrigaSignupSecondStep,
    StrigaSmsVerification,
    SumSubVerification;

    companion object {
        fun getValueFrom(userStatus: StrigaUserStatus?): BankTransferNavigationTarget {
            if (userStatus == null) return Nowhere
            return when {
                !strigaUserInteractor.isUserCreated() -> {
                    StrigaOnboarding
                }
                !userStatus.isMobileVerified -> {
                    StrigaSmsVerification
                }
                userStatus.kycStatus == StrigaUserVerificationStatus.INITIATED -> {
                    SumSubVerification
                }
                else -> {
                    // todo: on/off ramp
                    Nowhere
                }
            }
        }
    }
}
