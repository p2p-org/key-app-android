package org.p2p.wallet.home.analytics

import org.p2p.wallet.common.analytics.TrackerContract

class BrowseAnalytics(private val tracker: TrackerContract) {

    fun logTokenListViewed(lastScreenName: String, tokenListLocation: TokenListLocation) {
        tracker.logEvent(
            "Token_List_Viewed",
            arrayOf(
                Pair("Last_Screen", lastScreenName),
                Pair("Token_List_Location", tokenListLocation.title)
            )
        )
    }

    fun logTokenListScrolled(scrollDepth: String) {
        // TODO ask about scroll depth
        tracker.logEvent(
            "Token_List_Scrolled",
            arrayOf(
                Pair("Scroll_Depth", scrollDepth)
            )
        )
    }

    fun logTokenListSearching(searchString: String) {
        tracker.logEvent(
            "Token_List_Searching",
            arrayOf(
                Pair("Search_String", searchString)
            )
        )
    }

    fun logCurrencyListSearching(searchString: String) {
        tracker.logEvent(
            "Currency_List_Searching",
            arrayOf(
                Pair("Search_String", searchString)
            )
        )
    }

    fun logTokenChosen(tokenName: String) {
        tracker.logEvent(
            "Token_Chosen",
            arrayOf(
                Pair("Token_Name", tokenName)
            )
        )
    }

    fun logScreenOpened(screenName: String, lastScreen: String) {
        tracker.logEvent(
            "Screen_Opened",
            arrayOf(
                Pair("Screen_Name", screenName),
                Pair("Last_Screen", lastScreen)
            )
        )
    }

    fun logNetworkAdding() {
        tracker.logEvent("Network_Adding")
    }

    fun logNetworkChanging(networkName: String) {
        tracker.logEvent(
            "Network_Changing",
            arrayOf(
                Pair("Network_Name", networkName)
            )
        )
    }

    fun logNetworkSaving(networkName: String) {
        tracker.logEvent(
            "Network_Saved",
            arrayOf(
                Pair("Network_Name", networkName)
            )
        )
    }

    fun logBannerUsernamePressed() {
        tracker.logEvent("Banner_Username_Pressed")
    }

    fun logBannerBackupPressed() {
        tracker.logEvent("Banner_Backup_Pressed")
    }

    fun logBannerNotificationsPressed() {
        tracker.logEvent("Banner_Notifications_Pressed")
    }

    fun logBannerFeedbackPressed() {
        tracker.logEvent("Banner_Feedback_Pressed")
    }

    enum class TokenListLocation(val title: String) {
        SEND("Send"),
        BUY("Buy"),
        TOKEN_A("Token_A"),
        TOKEN_B("Token_B")
    }
}