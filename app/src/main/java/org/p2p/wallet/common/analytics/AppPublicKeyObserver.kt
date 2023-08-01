package org.p2p.wallet.common.analytics

import org.p2p.core.analytics.Analytics
import org.p2p.core.crashlytics.CrashLogger
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider

class AppPublicKeyObserver(
    private val tokenKeyProvider: TokenKeyProvider,
    private val analytics: Analytics,
    private val crashLogger: CrashLogger
) {

    fun startObserving() {
        tokenKeyProvider.registerListener {
            crashLogger.setUserId(it)
            analytics.setUserId(it.takeIf(String::isNotBlank))
        }
        runCatching { tokenKeyProvider.publicKey }
            .onSuccess {
                analytics.setUserId(it.takeIf(String::isNotBlank))
                crashLogger.setUserId(it)
            }
    }
}
