package org.p2p.core.crashlytics

import com.google.android.gms.common.api.ApiException
import okhttp3.internal.http2.ConnectionShutdownException
import okhttp3.internal.http2.StreamResetException
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException
import javax.net.ssl.SSLException
import javax.net.ssl.SSLHandshakeException
import kotlinx.coroutines.CancellationException
import org.p2p.core.analytics.repository.AnalyticsLocalRepository
import org.p2p.core.analytics.trackers.AmplitudeTracker
import org.p2p.core.network.data.ServerException

class AmplitudeErrorFacade(
    private val amplitudeTracker: AmplitudeTracker,
    private val analyticsLocalRepository: AnalyticsLocalRepository
) : CrashLoggingFacade {

    companion object {
        private const val EVENT_ERROR_NAME = "Client_Frontend_Error"

        private const val PARAMS_ERROR_VALUE = "Error_Value"
        private const val PARAMS_ERROR_FRAGMENT = "Error_Fragment"
    }

    override fun logInformation(information: String) {
        // do not log not critical information to amplitude
    }

    override fun logThrowable(error: Throwable, message: String?) {
        if (!isImportantError(error)) {
            // do not log non-critical errors
            return
        }

        amplitudeTracker.logEvent(
            eventName = EVENT_ERROR_NAME,
            params = mapOf(
                PARAMS_ERROR_VALUE to error.message.orEmpty(),
                PARAMS_ERROR_FRAGMENT to analyticsLocalRepository.getCurrentScreenName()
            )
        )
    }

    override fun setUserId(userId: String) {
        amplitudeTracker.setUserId(userId)
    }

    override fun clearUserId() {
        amplitudeTracker.clearUserProperties()
    }

    override fun setCustomKey(key: String, value: Any) {
        with(amplitudeTracker) {
            when (value) {
                is Int -> setCustomKey(key, value)
                is String -> setCustomKey(key, value)
                is Boolean -> setCustomKey(key, value)
                is Float -> setCustomKey(key, value)
                is Double -> setCustomKey(key, value)
                else -> setCustomKey(key, value.toString())
            }
        }
    }

    private fun isImportantError(throwable: Throwable): Boolean {
        return when (throwable) {
            is SocketTimeoutException,
            is SSLHandshakeException,
            is UnknownHostException,
            is SSLException,
            is TimeoutException,
            is ServerException,
            is HttpException,
            is CancellationException,
            is ApiException,
            is ConnectException,
            is OutOfMemoryError,
            is StreamResetException,
            is ConnectionShutdownException -> false
            else -> true
        }
    }
}
