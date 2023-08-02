package org.p2p.wallet.common.analytics

import timber.log.Timber
import org.p2p.core.analytics.Analytics
import org.p2p.core.crashlytics.CrashLogger
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider

class AppPublicKeyObserver(
    private val tokenKeyProvider: TokenKeyProvider,
    private val analytics: Analytics,
    private val crashLogger: CrashLogger
) {

    fun startObserving() {
        runCatching { tokenKeyProvider.publicKey }
            .onSuccess {
                Timber.i("Setting up public key for analytics and crash logger")
                analytics.setUserId(it.takeIf(String::isNotBlank))
                crashLogger.setUserId(it)
            }

        tokenKeyProvider.registerListener {
            crashLogger.setUserId(it)
            analytics.setUserId(it.takeIf(String::isNotBlank))
        }
    }
}
