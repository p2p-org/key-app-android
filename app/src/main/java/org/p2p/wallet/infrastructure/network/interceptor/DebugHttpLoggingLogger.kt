package org.p2p.wallet.infrastructure.network.interceptor

import com.google.gson.Gson
import com.google.gson.JsonParser
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber
import org.p2p.wallet.utils.invokeAndForget

private const val TAG = "_DebugLogger"

class DebugHttpLoggingLogger(
    private val gson: Gson,
    private val logTag: String
) : HttpLoggingInterceptor.Logger {

    override fun log(message: String) {
        // ignore SolanaApi logs from network - it's too big to fit in logcat
        if (logTag == "SolanaApi") {
            return
        }

        if (!message.startsWith('{') && !message.startsWith('[')) {
            Timber.tag(logTag + TAG).d(message)
            return
        }

        if (message.length > 10_000) {
            Timber.tag(logTag + TAG).e("Response body is too big (${message.length}), skipping")
            return
        }
        kotlin.runCatching {
            val parsedJson = JsonParser.parseString(message).let { gson.toJson(it) }
            Timber.tag(logTag + TAG).d(parsedJson)
        }
            .onFailure {
                with(Timber.tag(logTag + TAG)) {
                    d(message)
                    w(it)
                }
            }
            .invokeAndForget()
    }
}
