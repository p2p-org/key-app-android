package org.p2p.wallet.striga.user.storage

import org.p2p.core.utils.MillisSinceEpoch
import org.p2p.wallet.home.ui.wallet.mapper.model.StrigaKycStatusBanner
import org.p2p.wallet.striga.user.model.StrigaUserStatusDetails
import org.p2p.wallet.striga.wallet.models.StrigaCryptoAccountDetails
import org.p2p.wallet.striga.wallet.models.StrigaFiatAccountDetails
import org.p2p.wallet.striga.wallet.models.StrigaUserBankingDetails
import org.p2p.wallet.striga.wallet.models.StrigaUserWallet

interface StrigaStorageContract {
    var userStatus: StrigaUserStatusDetails?
    var userWallet: StrigaUserWallet?
    var fiatAccount: StrigaFiatAccountDetails?
    var cryptoAccount: StrigaCryptoAccountDetails?
    var bankingDetails: StrigaUserBankingDetails?
    var smsExceededVerificationAttemptsMillis: MillisSinceEpoch
    var smsExceededResendAttemptsMillis: MillisSinceEpoch

    fun hideBanner(banner: StrigaKycStatusBanner)
    fun isBannerHidden(banner: StrigaKycStatusBanner): Boolean

    fun clear()
}
