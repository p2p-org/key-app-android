package org.p2p.wallet.striga.signup.model

import org.p2p.wallet.striga.user.model.StrigaUserVerificationStatus

data class StrigaUserStatus(
    val isUserCreated: Boolean,
    val isMobileVerified: Boolean,
    val kycStatus: StrigaUserVerificationStatus
)
