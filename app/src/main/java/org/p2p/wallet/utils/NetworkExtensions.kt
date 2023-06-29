package org.p2p.wallet.utils

import okhttp3.Response
import retrofit2.HttpException
import timber.log.Timber
import java.net.UnknownHostException
import org.p2p.wallet.R
import org.p2p.core.network.data.ServerException

fun Throwable?.getErrorMessage(resourceDelegate: (res: Int) -> String): String =
    when (this) {
        is UnknownHostException -> resourceDelegate(R.string.error_network_message)
        is ServerException -> getDirectMessage() ?: resourceDelegate(R.string.error_general_message)
        else -> this?.message ?: resourceDelegate(R.string.error_general_message)
    }

fun retrofit2.Response<*>.errorBodyOrNull(): String? {
    return kotlin.runCatching { errorBody()?.string() }
        .onFailure { Timber.i(it) }
        .getOrNull()
}

fun HttpException.errorBodyOrNull(): String? {
    return response()?.errorBodyOrNull()
}
