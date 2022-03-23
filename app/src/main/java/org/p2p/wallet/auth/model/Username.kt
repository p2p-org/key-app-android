package org.p2p.wallet.auth.model

import android.content.Context
import org.p2p.wallet.R

data class Username(
    val username: String
) {

    fun getFullUsername(context: Context) = context.getString(R.string.auth_name_p2p_sol, username)
}
