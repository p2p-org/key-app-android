package org.p2p.wallet.auth.model

import android.content.Context
import org.p2p.wallet.R
import org.p2p.wallet.common.ResourcesProvider

data class Username(
    val value: String
) {

    fun getFullUsername(context: Context): String = context.getString(R.string.auth_name_p2p_sol, value)
    fun getFullUsername(provider: ResourcesProvider): String = provider.getString(R.string.auth_name_p2p_sol, value)
}
