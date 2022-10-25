package org.p2p.wallet.receive.analytics

import org.p2p.wallet.common.analytics.Analytics
import java.math.BigDecimal

private const val RECEIVE_VIEWED = "Receive_Viewed"
private const val RECEIVE_TOKEN_VIEWED = "Token_Receive_Viewed"
private const val RECEIVE_ADDRESS_COPIED = "Receive_Address_Copied"
private const val RECEIVE_QR_SAVED = "Receive_QR_Saved"
private const val RECEIVE_USERCARD_SHARED = "Receive_Usercard_Shared"
private const val RECEIVE_VIEWING_EXPLORER = "Receive_Viewing_Explorer"
private const val RECEIVE_CHANGING_NETWORK = "Receive_Changing_Network"
private const val RECEIVE_SETTING_BITCOIN = "Receive_Setting_Bitcoin"
private const val RECEIVE_NETWORK_CHANGED = "Receive_Network_Changed"
private const val RECEIVE_SHOWING_STATUSES = "Receive_Showing_Statuses"
private const val RECEIVE_SHOWING_STATUS = "Receive_Showing_Status"
private const val RECEIVE_SHOWING_HISTORY = "Receive_Showing_History"
private const val RECEIVE_SHOWING_DETAILS = "Receive_Showing_Details"
private const val RECEIVE_START_SCREEN = "Receive_Start_Screen"
private const val RECEIVE_ACTION_BUTTON = "Action_Button_Receive"

class ReceiveAnalytics(private val tracker: Analytics) {

    fun logReceiveViewed(isUsernameClaimed: Boolean) {
        tracker.logEvent(
            RECEIVE_VIEWED,
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
            RECEIVE_TOKEN_VIEWED,
            arrayOf(
                Pair("Token_Name", tokenName)
            )
        )
    }

    fun logReceiveAddressCopied(lastScreenName: String) {
        tracker.logEvent(
            RECEIVE_ADDRESS_COPIED,
            arrayOf(
                Pair("Last_Screen", lastScreenName)
            )
        )
    }

    fun logReceiveQrSaved(lastScreenName: String) {
        tracker.logEvent(
            RECEIVE_QR_SAVED,
            arrayOf(
                Pair("Last_Screen", lastScreenName)
            )
        )
    }

    fun logUserCardShared(lastScreenName: String) {
        tracker.logEvent(
            RECEIVE_USERCARD_SHARED,
            arrayOf(
                Pair("Last_Screen", lastScreenName)
            )
        )
    }

    fun logReceiveViewingExplorer(receiveNetwork: ReceiveNetwork) {
        tracker.logEvent(
            RECEIVE_VIEWING_EXPLORER,
            arrayOf(
                Pair("Receive_Network", receiveNetwork.title)
            )
        )
    }

    fun logReceiveChangingNetwork(receiveNetwork: ReceiveNetwork) {
        tracker.logEvent(
            RECEIVE_CHANGING_NETWORK,
            arrayOf(
                Pair("Receive_Network", receiveNetwork.title)
            )
        )
    }

    fun logReceiveSettingBitcoin() {
        tracker.logEvent(RECEIVE_SETTING_BITCOIN)
    }

    fun logReceiveNetworkChanged(receiveNetwork: ReceiveNetwork) {
        tracker.logEvent(
            RECEIVE_NETWORK_CHANGED,
            arrayOf(
                Pair("Receive_Network", receiveNetwork.title)
            )
        )
    }

    fun logReceiveShowingStatuses() {
        tracker.logEvent(RECEIVE_SHOWING_STATUSES)
    }

    fun logReceiveShowingStatus() {
        tracker.logEvent(RECEIVE_SHOWING_STATUS)
    }

    fun logReceiveShowingHistory() {
        tracker.logEvent(RECEIVE_SHOWING_HISTORY)
    }

    fun logReceiveShowingDetails(
        receiveSum: BigDecimal,
        receiveUSD: BigDecimal,
        tokenName: String,
        receiveNetwork: ReceiveNetwork
    ) {
        tracker.logEvent(
            RECEIVE_SHOWING_DETAILS,
            arrayOf(
                Pair("Receive_Sum", receiveSum),
                Pair("Receive_USD", receiveUSD),
                Pair("Token_Name", tokenName),
                Pair("Receive_Network", receiveNetwork.title)
            )
        )
    }

    fun logStartScreen(previousScreenName: String) {
        tracker.logEvent(
            event = RECEIVE_START_SCREEN,
            params = mapOf(
                "Last_Screen" to previousScreenName
            )
        )
    }

    fun logAddressOnMainClicked() {
        tracker.logEvent(
            event = "Main_Copy_Address"
        )
    }

    fun logReceiveActionButtonClicked() {
        tracker.logEvent(
            event = RECEIVE_ACTION_BUTTON
        )
    }

    enum class ReceiveNetwork(val title: String) {
        SOLANA("Solana"),
        BITCOIN("Bitcoin")
    }
}
