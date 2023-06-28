package org.p2p.wallet.striga.user

import androidx.core.content.edit
import android.content.SharedPreferences
import com.google.gson.Gson
import org.p2p.wallet.striga.user.model.StrigaUserStatusDetails
import org.p2p.wallet.utils.fromJsonReified

private const val KEY_USER_STATUS = "KEY_USER_STATUS"
private const val KEY_SMS_EXCEEDED_VERIFICATION_ATTEMPTS_MILLIS = "KEY_SMS_EXCEEDED_VERIFICATION_ATTEMPTS_MILLIS"
private const val KEY_SMS_EXCEEDED_RESEND_ATTEMPTS_MILLIS = "KEY_SMS_EXCEEDED_RESEND_ATTEMPTS_MILLIS"

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

    override var smsExceededVerificationAttemptsMillis: Long
        get() = sharedPreferences.getLong(KEY_SMS_EXCEEDED_VERIFICATION_ATTEMPTS_MILLIS, 0L)
        set(value) = sharedPreferences.edit(true) {
            putLong(KEY_SMS_EXCEEDED_VERIFICATION_ATTEMPTS_MILLIS, value)
        }

    override var smsExceededResendAttemptsMillis: Long
        get() = sharedPreferences.getLong(KEY_SMS_EXCEEDED_RESEND_ATTEMPTS_MILLIS, 0L)
        set(value) = sharedPreferences.edit(true) {
            putLong(KEY_SMS_EXCEEDED_RESEND_ATTEMPTS_MILLIS, value)
        }
}
