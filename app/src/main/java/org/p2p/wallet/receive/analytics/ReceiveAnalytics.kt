package org.p2p.wallet.receive.analytics

import org.p2p.wallet.common.analytics.Analytics
import java.math.BigDecimal

class ReceiveAnalytics(private val tracker: Analytics) {

    fun logReceiveViewed(isUsernameClaimed: Boolean) {
        tracker.logEvent(
            "Receive_Viewed",
            arrayOf(
                Pair("Username_Claimed", isUsernameClaimed)
            )
        )
    }

    fun logSettingsUsernameViewed(isUsernameClaimed: Boolean) {
        tracker.logEvent(
            "Settings_Username_Viewed",
            arrayOf(
                Pair("Username_Claimed", isUsernameClaimed)
            )
        )
    }

    fun logTokenReceiveViewed(tokenName: String) {
        tracker.logEvent(
            "Token_Receive_Viewed",
            arrayOf(
                Pair("Token_Name", tokenName)
            )
        )
    }

    fun logReceiveAddressCopied(lastScreenName: String) {
        tracker.logEvent(
            "Receive_Address_Copied",
            arrayOf(
                Pair("Last_Screen", lastScreenName)
            )
        )
    }

    fun logReceiveQrSaved(lastScreenName: String) {
        tracker.logEvent(
            "Receive_QR_Saved",
            arrayOf(
                Pair("Last_Screen", lastScreenName)
            )
        )
    }

    fun logUserCardShared(lastScreenName: String) {
        tracker.logEvent(
            "Receive_Usercard_Shared",
            arrayOf(
                Pair("Last_Screen", lastScreenName)
            )
        )
    }

    fun logReceiveViewingExplorer(receiveNetwork: ReceiveNetwork) {
        tracker.logEvent(
            "Receive_Viewing_Explorer",
            arrayOf(
                Pair("Receive_Network", receiveNetwork.title)
            )
        )
    }

    fun logReceiveChangingNetwork(receiveNetwork: ReceiveNetwork) {
        tracker.logEvent(
            "Receive_Changing_Network",
            arrayOf(
                Pair("Receive_Network", receiveNetwork.title)
            )
        )
    }

    fun logReceiveSettingBitcoin() {
        tracker.logEvent("Receive_Setting_Bitcoin")
    }

    fun logReceiveNetworkChanged(receiveNetwork: ReceiveNetwork) {
        tracker.logEvent(
            "Receive_Network_Changed",
            arrayOf(
                Pair("Receive_Network", receiveNetwork.title)
            )
        )
    }

    fun logReceiveShowingStatuses() {
        tracker.logEvent("Receive_Showing_Statuses")
    }

    fun logReceiveShowingStatus() {
        tracker.logEvent("Receive_Showing_Status")
    }

    fun logReceiveShowingHistory() {
        tracker.logEvent("Receive_Showing_History")
    }

    fun logReceiveShowingDetails(
        receiveSum: BigDecimal,
        receiveUSD: BigDecimal,
        tokenName: String,
        receiveNetwork: ReceiveNetwork
    ) {
        tracker.logEvent(
            "Receive_Showing_Details",
            arrayOf(
                Pair("Receive_Sum", receiveSum),
                Pair("Receive_USD", receiveUSD),
                Pair("Token_Name", tokenName),
                Pair("Receive_Network", receiveNetwork.title)
            )
        )
    }

    enum class ReceiveNetwork(val title: String) {
        SOLANA("Solana"),
        BITCOIN("Bitcoin")
    }
}
