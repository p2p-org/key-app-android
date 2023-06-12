package org.p2p.wallet.striga.user

import androidx.core.content.edit
import android.content.SharedPreferences
import com.google.gson.Gson
import org.p2p.wallet.striga.signup.model.StrigaUserStatus
import org.p2p.wallet.utils.fromJsonReified

private const val KEY_USER_STATUS = "KEY_USER_STATUS"

class StrigaStorage(
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson
) : StrigaStorageContract {

    override var userStatus: StrigaUserStatus?
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
}
