package org.p2p.wallet.utils

import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import org.p2p.wallet.R
import org.p2p.wallet.infrastructure.network.data.ServerException
import java.net.UnknownHostException

fun Throwable?.getErrorMessage(resourceDelegate: (res: Int) -> String): String =
    when (this) {
        is UnknownHostException -> resourceDelegate(R.string.error_network_message)
        is ServerException -> getDirectMessage() ?: resourceDelegate(R.string.error_general_message)
        else -> this?.message ?: resourceDelegate(R.string.error_general_message)
    }

fun Request.bodyAsString(): String = kotlin.runCatching {
    val requestCopy: Request = this.newBuilder().build()
    val buffer = Buffer()
    requestCopy.body?.writeTo(buffer)
    buffer.readUtf8()
}
    .getOrDefault("")

fun Response.bodyAsString(): String = peekBody(Long.MAX_VALUE).string()
