package org.p2p.wallet.utils

import android.content.Context
import okhttp3.Request
import okio.Buffer
import org.p2p.wallet.R
import org.p2p.wallet.common.ResourcesProvider
import org.p2p.wallet.infrastructure.network.data.ServerException
import java.net.UnknownHostException

fun Throwable?.getErrorMessage(resourcesProvider: ResourcesProvider): String =
    when (this) {
        is UnknownHostException -> resourcesProvider.getString(R.string.error_network_message)
        is ServerException -> getDirectMessage() ?: resourcesProvider.getString(R.string.error_general_message)
        else -> this?.message ?: resourcesProvider.getString(R.string.error_general_message)
    }

fun Throwable?.getErrorMessage(context: Context): String =
    when (this) {
        is UnknownHostException -> context.getString(R.string.error_network_message)
        is ServerException -> getDirectMessage() ?: context.getString(R.string.error_general_message)
        else -> this?.message ?: context.getString(R.string.error_general_message)
    }

fun Request.bodyAsString(): String = kotlin.runCatching {
    val requestCopy: Request = this.newBuilder().build()
    val buffer = Buffer()
    requestCopy.body?.writeTo(buffer)
    buffer.readUtf8()
}
    .getOrDefault("")
