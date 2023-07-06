package org.p2p.wallet.striga.user

import androidx.core.content.edit
import android.content.SharedPreferences
import com.google.gson.Gson
import org.p2p.core.utils.MillisSinceEpoch
import org.p2p.core.utils.fromJsonReified
import org.p2p.wallet.kyc.model.StrigaKycStatusBanner
import org.p2p.wallet.striga.user.model.StrigaUserStatusDetails

private const val KEY_USER_STATUS = "KEY_USER_STATUS"
private const val KEY_SMS_EXCEEDED_VERIFICATION_ATTEMPTS_MILLIS =
    "KEY_SMS_EXCEEDED_VERIFICATION_ATTEMPTS_MILLIS"
private const val KEY_SMS_EXCEEDED_RESEND_ATTEMPTS_MILLIS =
    "KEY_SMS_EXCEEDED_RESEND_ATTEMPTS_MILLIS"

private const val KEY_USER_BANNER_IS_HIDDEN_PREFIX = "KEY_USER_BANNER_IS_HIDDEN_PREFIX_"

class StrigaStorage(
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson
) : StrigaStorageContract {

    override var userStatus: StrigaUserStatusDetails?
        get() = sharedPreferences.getString(KEY_USER_STATUS, null)?.let(gson::fromJsonReified)
        set(value) {
            sharedPreferences.edit {
                if (value == null) {
                    remove(KEY_USER_STATUS)
                } else {
                    putString(KEY_USER_STATUS, gson.toJson(value))
                }
            }
        }

    override var smsExceededVerificationAttemptsMillis: MillisSinceEpoch
        get() = sharedPreferences.getLong(KEY_SMS_EXCEEDED_VERIFICATION_ATTEMPTS_MILLIS, 0L)
        set(value) = sharedPreferences.edit(true) {
            putLong(KEY_SMS_EXCEEDED_VERIFICATION_ATTEMPTS_MILLIS, value)
        }

    override var smsExceededResendAttemptsMillis: MillisSinceEpoch
        get() = sharedPreferences.getLong(KEY_SMS_EXCEEDED_RESEND_ATTEMPTS_MILLIS, 0L)
        set(value) = sharedPreferences.edit(true) {
            putLong(KEY_SMS_EXCEEDED_RESEND_ATTEMPTS_MILLIS, value)
        }

    override fun hideBanner(banner: StrigaKycStatusBanner) {
        sharedPreferences.edit(true) {
            putBoolean(KEY_USER_BANNER_IS_HIDDEN_PREFIX + banner.name, true)
        }
    }

    override fun isBannerHidden(banner: StrigaKycStatusBanner): Boolean {
        return sharedPreferences.getBoolean(KEY_USER_BANNER_IS_HIDDEN_PREFIX + banner.name, false)
    }

    override fun clear() {
        sharedPreferences.edit { clear() }
    }
}
