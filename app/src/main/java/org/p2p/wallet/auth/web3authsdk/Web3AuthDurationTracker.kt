package org.p2p.wallet.auth.web3authsdk

import timber.log.Timber
import java.util.Date
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import org.p2p.wallet.auth.analytics.OnboardingAnalytics
import org.p2p.wallet.infrastructure.network.environment.TorusEnvironment
import org.p2p.wallet.utils.DateTimeUtils.PATTERN_HH_MM_SS_SS
import org.p2p.wallet.utils.DateTimeUtils.getFormattedDate
import org.p2p.wallet.utils.emptyString

private const val EXPECTED_REQUEST_TIME_SEC = 15
private const val TAG = "Web3AuthDuration"

class Web3AuthDurationTracker(
    private val analytics: OnboardingAnalytics,
    private val torusNetwork: TorusEnvironment,
) {

    private class Web3AuthTakesTooLong(
        methodName: String,
        duration: Duration
    ) : Throwable(message = "Web3Auth::$methodName took ${duration.inWholeSeconds}s to response")

    private var methodName: String = emptyString()
    private var methodDurationStart: Duration = Duration.ZERO

    fun startMethodCall(methodName: String) {
        val now = System.nanoTime().toDuration(DurationUnit.NANOSECONDS)
        this.methodName = methodName
        this.methodDurationStart = now

        Timber.tag(TAG).i(
            buildString {
                append("--> Web3Auth request: ")
                append("$methodName; ")
                append("${torusNetwork.verifier}; ")
                append("${torusNetwork.subVerifier}; ")
                append("${torusNetwork.baseUrl}; ")
                append("date=${getFormattedDate(Date().time, PATTERN_HH_MM_SS_SS)}")
            }
        )
    }

    fun finishLastMethodCall(isMethodReturnedError: Boolean = false) {
        if (methodName.isBlank() || methodDurationStart == Duration.ZERO) {
            Timber.e(IllegalArgumentException("Web3AuthDuration works improperly: methodName=$methodName;"))
            return
        }

        val methodDurationFinish = System.nanoTime().toDuration(DurationUnit.NANOSECONDS)
        val methodDurationFinishTimeStamp = Date().time
        val tookDuration = methodDurationFinish - methodDurationStart
        Timber.tag(TAG).i(
            buildString {
                append("<-- Web3Auth response: $methodName;")
                append("error=$isMethodReturnedError;")
                append("date=${getFormattedDate(methodDurationFinishTimeStamp, PATTERN_HH_MM_SS_SS)};")
                append("took=${tookDuration.inWholeSeconds}s${tookDuration.inWholeMilliseconds}ms")
            }
        )

        analytics.logTorusRequestResponseTime(methodName, tookDuration)

        if (tookDuration.inWholeSeconds > EXPECTED_REQUEST_TIME_SEC) {
            Timber.e(Web3AuthTakesTooLong(methodName, tookDuration))
        }

        methodName = emptyString()
        methodDurationStart = Duration.ZERO
    }
}
