package org.p2p.wallet.settings.ui.settings

import org.p2p.wallet.auth.analytics.AdminAnalytics
import org.p2p.wallet.home.analytics.BrowseAnalytics
import org.p2p.wallet.receive.analytics.ReceiveAnalytics

class SettingsPresenterAnalytics(
    private val receiveAnalytics: ReceiveAnalytics,
    private val adminAnalytics: AdminAnalytics,
    private val browseAnalytics: BrowseAnalytics,
) {
    fun logSignOut() {
        adminAnalytics.logSignOut()
    }

    fun logSignedOut() {
        adminAnalytics.logSignedOut()
    }

    fun logSettingsUsernameViewed(usernameExists: Boolean) {
        receiveAnalytics.logSettingsUsernameViewed(usernameExists)
    }

    fun logNetworkChanging(name: String) {
        browseAnalytics.logNetworkChanging(name)
    }
}
