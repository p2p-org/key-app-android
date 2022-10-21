package org.p2p.wallet.auth.ui.reserveusername

import org.p2p.wallet.R
import org.p2p.wallet.common.ResourcesProvider

class UsernameValidator(resourcesProvider: ResourcesProvider) {
    private val usernameMinimalLength: Int =
        resourcesProvider.getInteger(R.integer.reserve_username_input_view_min_length)

    private val usernameMaximalLength: Int =
        resourcesProvider.getInteger(R.integer.reserve_username_input_view_max_length)

    private val usernameAllowedSymbols: String =
        resourcesProvider.getString(R.string.common_username_allowed_symbols)

    fun isUsernameValid(username: String): Boolean {
        return username.length in usernameMinimalLength..usernameMaximalLength &&
            username.all { usernameSymbol -> usernameSymbol in usernameAllowedSymbols }
    }
}
