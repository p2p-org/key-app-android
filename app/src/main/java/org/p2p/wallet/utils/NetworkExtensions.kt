package org.p2p.wallet.utils

import android.content.Context
import org.p2p.wallet.R
import org.p2p.wallet.infrastructure.network.data.ServerException
import java.net.UnknownHostException

fun Throwable?.getErrorMessage(context: Context): String =
    when (this) {
        is UnknownHostException -> context.getString(R.string.error_network_message)
        is ServerException -> getDirectMessage() ?: context.getString(R.string.error_general_message)
        else -> this?.message ?: context.getString(R.string.error_general_message)
    }