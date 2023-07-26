package org.p2p.wallet.striga.user.storage

import org.p2p.core.utils.MillisSinceEpoch
import org.p2p.wallet.common.EncryptedSharedPreferences
import org.p2p.wallet.common.LongPreference
import org.p2p.wallet.common.ObjectEncryptedPreference
import org.p2p.wallet.home.ui.wallet.mapper.model.StrigaKycStatusBanner
import org.p2p.wallet.striga.user.model.StrigaUserStatusDetails
import org.p2p.wallet.striga.wallet.models.StrigaCryptoAccountDetails
import org.p2p.wallet.striga.wallet.models.StrigaFiatAccountDetails
import org.p2p.wallet.striga.wallet.models.StrigaUserBankingDetails
import org.p2p.wallet.striga.wallet.models.StrigaUserWallet

private const val KEY_USER_STATUS =
    "KEY_USER_STATUS"
private const val KEY_USER_WALLET =
    "KEY_USER_WALLET"
private const val KEY_FIAT_ACCOUNT_DETAILS =
    "KEY_FIAT_ACCOUNT_DETAILS"
private const val KEY_CRYPTO_ACCOUNT_DETAILS =
    "KEY_CRYPTO_ACCOUNT_DETAILS"
private const val KEY_SMS_EXCEEDED_VERIFICATION_ATTEMPTS_MILLIS =
    "KEY_SMS_EXCEEDED_VERIFICATION_ATTEMPTS_MILLIS"
private const val KEY_SMS_EXCEEDED_RESEND_ATTEMPTS_MILLIS =
    "KEY_SMS_EXCEEDED_RESEND_ATTEMPTS_MILLIS"
private const val KEY_USER_BANNER_IS_HIDDEN =
    "KEY_USER_BANNER_IS_HIDDEN"
private const val KEY_USER_BANKING_DETAILS =
    "KEY_USER_BANKING_DETAILS"

class StrigaStorage(
    private val encryptedPrefs: EncryptedSharedPreferences,
) : StrigaStorageContract {

    override var userStatus: StrigaUserStatusDetails? by ObjectEncryptedPreference(
        preferences = encryptedPrefs,
        keyProvider = { KEY_USER_STATUS },
        type = StrigaUserStatusDetails::class,
        nullIfMappingFailed = false
    )

    override var userWallet: StrigaUserWallet? by ObjectEncryptedPreference(
        preferences = encryptedPrefs,
        keyProvider = { KEY_USER_WALLET },
        type = StrigaUserWallet::class,
        nullIfMappingFailed = true
    )

    override var fiatAccount: StrigaFiatAccountDetails? by ObjectEncryptedPreference(
        preferences = encryptedPrefs,
        keyProvider = { KEY_FIAT_ACCOUNT_DETAILS },
        type = StrigaFiatAccountDetails::class,
        nullIfMappingFailed = true
    )

    override var cryptoAccount: StrigaCryptoAccountDetails? by ObjectEncryptedPreference(
        preferences = encryptedPrefs,
        keyProvider = { KEY_CRYPTO_ACCOUNT_DETAILS },
        type = StrigaCryptoAccountDetails::class,
        nullIfMappingFailed = true
    )

    override var bankingDetails: StrigaUserBankingDetails? by ObjectEncryptedPreference(
        preferences = encryptedPrefs,
        keyProvider = { KEY_USER_BANKING_DETAILS },
        type = StrigaUserBankingDetails::class,
        nullIfMappingFailed = true
    )

    override var smsExceededVerificationAttemptsMillis: MillisSinceEpoch by LongPreference(
        preferences = encryptedPrefs,
        keyProvider = { KEY_SMS_EXCEEDED_VERIFICATION_ATTEMPTS_MILLIS },
        defaultValue = 0
    )

    override var smsExceededResendAttemptsMillis: MillisSinceEpoch by LongPreference(
        preferences = encryptedPrefs,
        keyProvider = { KEY_SMS_EXCEEDED_RESEND_ATTEMPTS_MILLIS },
        defaultValue = 0
    )

    private val hiddenBannersIds: List<Int>
        get() = encryptedPrefs.getStringSet(KEY_USER_BANNER_IS_HIDDEN)
            .map(String::toInt)

    override fun hideBanner(banner: StrigaKycStatusBanner) {
        val updatedHiddenBanners = hiddenBannersIds
            .plus(banner.bannerId)
            .map { it.toString() }
            .toSet()
        encryptedPrefs.saveStringSet(KEY_USER_BANNER_IS_HIDDEN, updatedHiddenBanners)
    }

    override fun isBannerHidden(banner: StrigaKycStatusBanner): Boolean {
        return banner.bannerId in hiddenBannersIds
    }

    override fun clear() {
        userStatus = null
        userWallet = null
        fiatAccount = null
        cryptoAccount = null
        bankingDetails = null
        smsExceededVerificationAttemptsMillis = 0
        smsExceededResendAttemptsMillis = 0

        encryptedPrefs.remove(KEY_USER_BANNER_IS_HIDDEN)
    }
}
