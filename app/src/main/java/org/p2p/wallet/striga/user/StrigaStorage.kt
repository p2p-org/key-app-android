package org.p2p.wallet.striga.user

import org.p2p.core.utils.MillisSinceEpoch
import org.p2p.wallet.common.EncryptedSharedPreferences
import org.p2p.wallet.common.LongPreference
import org.p2p.wallet.common.ObjectEncryptedPreference
import org.p2p.wallet.striga.user.model.StrigaUserStatusDetails
import org.p2p.wallet.striga.wallet.models.StrigaFiatAccountDetails
import org.p2p.wallet.striga.wallet.models.StrigaUserWallet

private const val KEY_USER_STATUS =
    "KEY_USER_STATUS"
private const val KEY_USER_WALLET =
    "KEY_USER_STATUS"
private const val KEY_FIAT_ACCOUNT_DETAILS =
    "KEY_FIAT_ACCOUNT_DETAILS"
private const val KEY_SMS_EXCEEDED_VERIFICATION_ATTEMPTS_MILLIS =
    "KEY_SMS_EXCEEDED_VERIFICATION_ATTEMPTS_MILLIS"
private const val KEY_SMS_EXCEEDED_RESEND_ATTEMPTS_MILLIS =
    "KEY_SMS_EXCEEDED_RESEND_ATTEMPTS_MILLIS"

class StrigaStorage(
    encryptedPrefs: EncryptedSharedPreferences,
) : StrigaStorageContract {

    override var userStatus: StrigaUserStatusDetails? by ObjectEncryptedPreference(
        preferences = encryptedPrefs,
        key = KEY_USER_STATUS,
        type = StrigaUserStatusDetails::class,
        nullIfMappingFailed = true
    )

    override var userWallet: StrigaUserWallet? by ObjectEncryptedPreference(
        preferences = encryptedPrefs,
        key = KEY_USER_WALLET,
        type = StrigaUserWallet::class,
        nullIfMappingFailed = true
    )

    override var fiatAccount: StrigaFiatAccountDetails? by ObjectEncryptedPreference(
        preferences = encryptedPrefs,
        key = KEY_FIAT_ACCOUNT_DETAILS,
        type = StrigaFiatAccountDetails::class,
        nullIfMappingFailed = true
    )

    override var smsExceededVerificationAttemptsMillis: MillisSinceEpoch by LongPreference(
        preferences = encryptedPrefs,
        key = KEY_SMS_EXCEEDED_VERIFICATION_ATTEMPTS_MILLIS,
        defaultValue = 0
    )

    override var smsExceededResendAttemptsMillis: MillisSinceEpoch by LongPreference(
        preferences = encryptedPrefs,
        key = KEY_SMS_EXCEEDED_RESEND_ATTEMPTS_MILLIS,
        defaultValue = 0
    )
}
