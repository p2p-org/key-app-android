package org.p2p.wallet.infrastructure.network.interceptor

import com.google.gson.Gson
import com.google.gson.JsonParser
import okhttp3.logging.HttpLoggingInterceptor
import org.p2p.wallet.utils.invokeAndForget
import timber.log.Timber

class DebugHttpLoggingLogger(
    private val gson: Gson,
    private val logTag: String
) : HttpLoggingInterceptor.Logger {
    override fun log(message: String) {
        if (!message.startsWith('{') && !message.startsWith('[')) {
            Timber.tag(logTag).d(message)
            return
        }

        kotlin.runCatching {
            val parsedJson = JsonParser.parseString(message).let { gson.toJson(it) }
            Timber.tag(logTag).d(parsedJson)
        }
            .onFailure {
                with(Timber.tag(logTag)) {
                    d(message)
                    w(it)
                }
            }
            .invokeAndForget()
    }
}
