package org.p2p.wallet.home.analytics

import org.p2p.core.analytics.Analytics
import org.p2p.core.analytics.constants.EventNames.BROWSE_BANNER_BACKUP_PRESSED
import org.p2p.core.analytics.constants.EventNames.BROWSE_BANNER_FEEDBACK_PRESSED
import org.p2p.core.analytics.constants.EventNames.BROWSE_BANNER_NOTIFICATION_PRESSED
import org.p2p.core.analytics.constants.EventNames.BROWSE_BANNER_USERNAME_PRESSED
import org.p2p.core.analytics.constants.EventNames.BROWSE_CURRENCY_LIST_SEARCHING
import org.p2p.core.analytics.constants.EventNames.BROWSE_NETWORK_ADDING
import org.p2p.core.analytics.constants.EventNames.BROWSE_NETWORK_CHANGING
import org.p2p.core.analytics.constants.EventNames.BROWSE_NETWORK_SAVED
import org.p2p.core.analytics.constants.EventNames.BROWSE_SCREEN_OPENED
import org.p2p.core.analytics.constants.EventNames.BROWSE_TOKEN_CHOSEN
import org.p2p.core.analytics.constants.EventNames.BROWSE_TOKEN_LIST_SCROLLED
import org.p2p.core.analytics.constants.EventNames.BROWSE_TOKEN_LIST_SEARCHED
import org.p2p.core.analytics.constants.EventNames.BROWSE_TOKEN_LIST_VIEWED
import org.p2p.core.common.di.AppScope
import kotlinx.coroutines.launch

class BrowseAnalytics(
    private val tracker: Analytics,
    private val appScope: AppScope,
) {

    fun logTokenListViewed(lastScreenName: String, tokenListLocation: TokenListLocation) {
        tracker.logEvent(
            BROWSE_TOKEN_LIST_VIEWED,
            arrayOf(
                Pair("Last_Screen", lastScreenName),
                Pair("Token_List_Location", tokenListLocation.title)
            )
        )
    }

    fun logTokenListScrolled(scrollDepth: String) {
        // TODO ask about scroll depth
        tracker.logEvent(
            BROWSE_TOKEN_LIST_SCROLLED,
            arrayOf(
                Pair("Scroll_Depth", scrollDepth)
            )
        )
    }

    fun logTokenListSearching(searchString: String) {
        tracker.logEvent(
            BROWSE_TOKEN_LIST_SEARCHED,
            arrayOf(
                Pair("Search_String", searchString)
            )
        )
    }

    fun logCurrencyListSearching(searchString: String) {
        tracker.logEvent(
            BROWSE_CURRENCY_LIST_SEARCHING,
            arrayOf(
                Pair("Search_String", searchString)
            )
        )
    }

    fun logTokenChosen(tokenName: String) {
        tracker.logEvent(
            BROWSE_TOKEN_CHOSEN,
            arrayOf(
                Pair("Token_Name", tokenName)
            )
        )
    }

    fun logScreenOpened(
        screenName: String,
        lastScreen: String,
        isSellEnabled: Boolean
    ) {
        appScope.launch {
            tracker.logEvent(
                event = BROWSE_SCREEN_OPENED,
                params = mapOf(
                    "Screen_Name" to screenName,
                    "Last_Screen" to lastScreen,
                    "Is_Sell_Enabled" to isSellEnabled
                )
            )
        }
    }

    fun logNetworkAdding() {
        tracker.logEvent(BROWSE_NETWORK_ADDING)
    }

    fun logNetworkChanging(networkName: String) {
        tracker.logEvent(
            BROWSE_NETWORK_CHANGING,
            arrayOf(
                Pair("Network_Name", networkName)
            )
        )
    }

    fun logNetworkSaving(networkName: String) {
        tracker.logEvent(
            BROWSE_NETWORK_SAVED,
            arrayOf(
                Pair("Network_Name", networkName)
            )
        )
    }

    fun logBannerUsernamePressed() {
        tracker.logEvent(BROWSE_BANNER_USERNAME_PRESSED)
    }

    fun logBannerBackupPressed() {
        tracker.logEvent(BROWSE_BANNER_BACKUP_PRESSED)
    }

    fun logBannerNotificationsPressed() {
        tracker.logEvent(BROWSE_BANNER_NOTIFICATION_PRESSED)
    }

    fun logBannerFeedbackPressed() {
        tracker.logEvent(BROWSE_BANNER_FEEDBACK_PRESSED)
    }

    enum class TokenListLocation(val title: String) {
        SEND("Send"),
        BUY("Buy"),
        TOKEN_A("Token_A"),
        TOKEN_B("Token_B"),
        RECEIVE("Receive")
    }
}
