package org.p2p.wallet.striga.user.model

data class StrigaUserInitialDetails(
    val userId: String,
    val email: String,
    val kycStatus: StrigaUserInitialKycDetails
)

class StrigaUserInitialKycDetails(
    val status: StrigaUserVerificationStatus
)
