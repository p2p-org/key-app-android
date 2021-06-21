package com.p2p.wallet.utils

import android.content.Context
import com.p2p.wallet.R
import com.p2p.wallet.infrastructure.network.ServerException
import java.net.UnknownHostException

fun Throwable?.getErrorMessage(context: Context): String =
    when (this) {
        is UnknownHostException -> context.getString(R.string.error_network_message)
        is ServerException -> getErrorMessage(context) ?: context.getString(R.string.error_general_message)
        else -> context.getString(R.string.error_general_message)
    }