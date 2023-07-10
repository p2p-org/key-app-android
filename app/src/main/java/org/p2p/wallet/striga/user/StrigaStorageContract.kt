package org.p2p.wallet.striga.user

import org.p2p.core.utils.MillisSinceEpoch
import org.p2p.wallet.kyc.model.StrigaKycStatusBanner
import org.p2p.wallet.striga.user.model.StrigaUserStatusDetails
import org.p2p.wallet.striga.wallet.models.StrigaFiatAccountDetails
import org.p2p.wallet.striga.wallet.models.StrigaUserWallet

interface StrigaStorageContract {
    var userStatus: StrigaUserStatusDetails?
    var userWallet: StrigaUserWallet?
    var fiatAccount: StrigaFiatAccountDetails?
    var smsExceededVerificationAttemptsMillis: MillisSinceEpoch
    var smsExceededResendAttemptsMillis: MillisSinceEpoch

    fun hideBanner(banner: StrigaKycStatusBanner)
    fun isBannerHidden(banner: StrigaKycStatusBanner): Boolean

    fun clear()
}
