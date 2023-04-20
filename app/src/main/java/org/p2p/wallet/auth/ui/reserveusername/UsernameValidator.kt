package org.p2p.wallet.auth.ui.reserveusername

import org.p2p.wallet.R
import android.content.res.Resources

class UsernameValidator(resources: Resources) {
    private val usernameMinimalLength: Int =
        resources.getInteger(R.integer.reserve_username_input_view_min_length)

    private val usernameMaximalLength: Int =
        resources.getInteger(R.integer.reserve_username_input_view_max_length)

    private val usernameAllowedSymbols: String =
        resources.getString(R.string.common_username_allowed_symbols)

    fun isUsernameValid(username: String): Boolean {
        return username.length in usernameMinimalLength..usernameMaximalLength &&
            username.all { usernameSymbol -> usernameSymbol in usernameAllowedSymbols }
    }
}
