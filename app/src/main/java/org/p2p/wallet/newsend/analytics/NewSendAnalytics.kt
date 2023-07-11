package org.p2p.wallet.newsend.analytics

import java.math.BigDecimal
import org.p2p.core.model.CurrencyMode
import org.p2p.wallet.common.analytics.Analytics
import org.p2p.wallet.newsend.model.SearchResult
import org.p2p.wallet.newsend.ui.SearchOpenedFromScreen

private const val NEW_SEND_RECIPIENT_VIEWED = "Send_New_Recipient_Screen"
private const val NEW_SEND_RECIPIENT_ADD = "Send_New_Recipient_Add"
private const val NEW_SEND_VIEWED = "Send_New_Input_Screen"
private const val NEW_SEND_FREE_TRANSACTIONS_CLICK = "Send_New_Free_Transaction_Click"
private const val NEW_SEND_TOKEN_SELECTION_CLICK = "Send_New_Token_Input_Click"
private const val NEW_SEND_SWITCH_CURRENCY_MODE_CLICK = "Send_New_Fiat_Input_Click"
private const val NEW_SEND_CONFIRM_BUTTON_CLICK = "Send_New_Confirm_Button_Click"
private const val NEW_SEND_ACTION_BUTTON = "Action_Button_Send"
private const val NEW_SEND_HOME_BAR_BUTTON = "Main_Screen_Send_Bar"
private const val NEW_SEND_SHOWING_DETAILS = "Send_Showing_Details"
private const val NEW_SEND_QR_GOING_BACK = "Send_QR_Going_Back"

private const val SEND_START_SCREEN_OPEN = "Send_Start_Screen_Open"
private const val SEND_TOKEN_SCREEN_ACTION_CLICKED = "Token_Screen_Send_Bar"

class NewSendAnalytics(
    private val analytics: Analytics
) {

    private var isMaxButtonClicked: Boolean = false

    fun logTokenScreenActionClicked(flow: AnalyticsSendFlow) {
        analytics.logEvent(
            SEND_TOKEN_SCREEN_ACTION_CLICKED
        )
    }

    fun logSearchScreenOpened(openedFrom: SearchOpenedFromScreen) {
        analytics.logEvent(
            event = SEND_START_SCREEN_OPEN,
            params = mapOf(
                "Last_Screen" to when (openedFrom) {
                    SearchOpenedFromScreen.ACTION_PANEL -> "Action_Panel"
                    SearchOpenedFromScreen.MAIN -> "Tap_Main"
                }
            )
        )
    }

    fun logNewSendScreenOpened(flow: AnalyticsSendFlow) {
        analytics.logEvent(
            event = NEW_SEND_VIEWED,
            params = mapOf(
                "Send_Flow" to flow.title
            )
        )
    }

    fun logFreeTransactionsClicked(flow: AnalyticsSendFlow) {
        analytics.logEvent(
            event = NEW_SEND_FREE_TRANSACTIONS_CLICK,
            params = mapOf(
                "Send_Flow" to flow.title
            )
        )
    }

    fun logTokenSelectionClicked(flow: AnalyticsSendFlow) {
        analytics.logEvent(
            event = NEW_SEND_TOKEN_SELECTION_CLICK,
            params = mapOf(
                "Send_Flow" to flow.title
            )
        )
    }

    fun logSwitchCurrencyModeClicked(mode: CurrencyMode, flow: AnalyticsSendFlow) {
        analytics.logEvent(
            event = NEW_SEND_SWITCH_CURRENCY_MODE_CLICK,
            params = mapOf(
                "Crypto" to (mode is CurrencyMode.Token),
                "Send_Flow" to flow.title
            )
        )
    }

    fun logSendConfirmButtonClicked(
        tokenName: String,
        amountInToken: String,
        amountInUsd: String,
        isFeeFree: Boolean,
        mode: CurrencyMode,
        flow: AnalyticsSendFlow
    ) {
        analytics.logEvent(
            event = NEW_SEND_CONFIRM_BUTTON_CLICK,
            params = mapOf(
                "Token" to tokenName,
                "Max" to isMaxButtonClicked,
                "Amount_Token" to amountInToken,
                "Amount_USD" to amountInUsd,
                "Fee" to isFeeFree,
                "Fiat_Input" to (mode is CurrencyMode.Fiat),
                "Send_Flow" to flow.title
            )
        )
    }

    fun logNewSearchScreenOpened() {
        analytics.logEvent(event = NEW_SEND_RECIPIENT_VIEWED)
    }

    fun logRecipientSelected(recipient: SearchResult, foundResult: List<SearchResult>) {
        val recipientType = when (recipient) {
            is SearchResult.UsernameFound -> "Username"
            is SearchResult.AddressFound -> "Address"
            else -> return
        }

        val type = if (foundResult.any { it == recipient }) {
            RecipientSelectionType.SEARCH
        } else {
            RecipientSelectionType.RECENT
        }
        analytics.logEvent(
            event = NEW_SEND_RECIPIENT_ADD,
            params = mapOf(
                "Recipient_With" to recipientType,
                "Added_By" to type.type
            )
        )
    }

    fun setMaxButtonClicked(isClicked: Boolean) {
        isMaxButtonClicked = isClicked
    }

    fun logSendQrGoingBack(
        qrCameraIsAvailable: Boolean,
        qrGalleryIsAvailable: Boolean,
        qrTab: QrTab
    ) {
        analytics.logEvent(
            NEW_SEND_QR_GOING_BACK,
            mapOf(
                "QR_Camera_Availability" to qrCameraIsAvailable,
                "QR_Gallery_Availability" to qrGalleryIsAvailable,
                "QR_Tab" to qrTab.title,
            )
        )
    }

    fun logSendActionButtonClicked() {
        analytics.logEvent(NEW_SEND_ACTION_BUTTON)
    }

    fun logSendHomeBarClicked() {
        analytics.logEvent(NEW_SEND_HOME_BAR_BUTTON)
    }

    fun logSendShowingDetails(
        sendStatus: SendStatus,
        lastScreenName: String,
        tokenName: String,
        sendNetwork: AnalyticsSendNetwork,
        sendSum: BigDecimal,
        sendUSD: BigDecimal
    ) {
        analytics.logEvent(
            NEW_SEND_SHOWING_DETAILS,
            mapOf(
                "Send_Status" to sendStatus.title,
                "Last_Screen" to lastScreenName,
                "Token_Name" to tokenName,
                "Send_Network" to sendNetwork.title,
                "Send_Sum" to sendSum,
                "Send_USD" to sendUSD
            )
        )
    }

    enum class RecipientSelectionType(val type: String) {
        RECENT("Recent"),
        SEARCH("Search")
    }

    enum class AnalyticsSendNetwork(val title: String) {
        SOLANA("Solana"),
        ETHEREUM("Ethereum")
    }

    enum class AnalyticsSendFlow(val title: String) {
        SEND("Send"),
        BRIDGE("Bridge"),
        SEND_VIA_LINK("Send_Via_Link"),
        SELL("Sell")
    }

    enum class SendStatus(val title: String) {
        SUCCESS("Success"),
        PENDING("Pending"),
        ERROR("Error")
    }

    enum class QrTab(val title: String) {
        CAMERA("Camera"),
        GALLERY("Gallery")
    }
}
