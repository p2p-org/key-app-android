package org.p2p.wallet.auth.model

import org.p2p.wallet.R
import org.p2p.wallet.common.ResourcesProvider

data class Username(val username: String) {
    fun getFullUsername(provider: ResourcesProvider): String = provider.getString(R.string.auth_name_key_sol, username)
}
