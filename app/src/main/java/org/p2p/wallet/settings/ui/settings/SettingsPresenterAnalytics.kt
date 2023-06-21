package org.p2p.wallet.settings.ui.settings

import org.p2p.wallet.auth.analytics.AdminAnalytics
import org.p2p.wallet.common.analytics.Analytics
import org.p2p.wallet.home.analytics.BrowseAnalytics
import org.p2p.wallet.receive.analytics.ReceiveAnalytics

class SettingsPresenterAnalytics(
    private val receiveAnalytics: ReceiveAnalytics,
    private val adminAnalytics: AdminAnalytics,
    private val browseAnalytics: BrowseAnalytics,
    private val tracker: Analytics
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

    fun logSettingItemClicked(itemName: String) {
        tracker.logEvent(itemName)
    }

    companion object {
        const val SETTING_ITEM_USERNAME = "Settings_Username_Click"
        const val SETTING_ITEM_PIN = "Settings_Pin_Click"
        const val SETTING_ITEM_SECURITY = "Settings_Security_Click"
        const val SETTING_ITEM_NETWORK = "Settings_Network_Click"
        const val SETTING_ITEM_SUPPORT = "Settings_Support_Click"
        const val SETTING_ITEM_TWITTER = "Settings_Twitter_Click"
        const val SETTING_ITEM_DISCORD = "Settings_Discord_Click"
        const val SETTING_ITEM_HIDE_BALANCE = "Settings_Hide_Balances_Click"
    }
}
