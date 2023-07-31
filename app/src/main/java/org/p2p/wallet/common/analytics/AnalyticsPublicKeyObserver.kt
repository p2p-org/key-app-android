package org.p2p.wallet.common.analytics

import org.p2p.core.analytics.Analytics
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider

class AnalyticsPublicKeyObserver(
    private val tokenKeyProvider: TokenKeyProvider,
    private val analytics: Analytics
) {

    fun startObserving() {
        tokenKeyProvider.registerListener {
            analytics.setUserId(it.takeIf(String::isNotBlank))
        }
        kotlin.runCatching {
            if (tokenKeyProvider.publicKey.isNotBlank()) {
                analytics.setUserId(tokenKeyProvider.publicKey)
            }
        }
    }
}
