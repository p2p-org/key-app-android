package org.p2p.wallet.newsend.analytics

import org.p2p.core.model.CurrencyMode
import org.p2p.wallet.common.analytics.Analytics
import org.p2p.wallet.newsend.model.NetworkType
import org.p2p.wallet.newsend.model.SearchResult
import java.math.BigDecimal

const val NEW_SEND_RECIPIENT_VIEWED = "Sendnew_Recipient_Screen"
const val NEW_SEND_RECIPIENT_ADD = "Sendnew_Recipient_Add"
const val NEW_SEND_VIEWED = "Sendnew_Input_Screen"
const val NEW_SEND_FREE_TRANSACTIONS_CLICK = "Sendnew_Free_Transaction_Click"
const val NEW_SEND_TOKEN_SELECTION_CLICK = "Sendnew_Token_Input_Click"
const val NEW_SEND_SWITCH_CURRENCY_MODE_CLICK = "Sendnew_Fiat_Input_Click"
const val NEW_SEND_CONFIRM_BUTTON_CLICK = "Sendnew_Confirm_Button_Click"
const val NEW_SEND_ACTION_BUTTON = "Action_Button_Send"
const val NEW_SEND_SHOWING_DETAILS = "Send_Showing_Details"
const val NEW_SEND_QR_GOING_BACK = "Send_QR_Going_Back"

class NewSendAnalytics(
    private val analytics: Analytics
) {

    private var isMaxButtonClicked: Boolean = false

    fun logNewSendScreenOpened() {
        analytics.logEvent(event = NEW_SEND_VIEWED)
    }

    fun logFreeTransactionsClicked() {
        analytics.logEvent(event = NEW_SEND_FREE_TRANSACTIONS_CLICK)
    }

    fun logTokenSelectionClicked() {
        analytics.logEvent(event = NEW_SEND_TOKEN_SELECTION_CLICK)
    }

    fun logSwitchCurrencyModeClicked(mode: CurrencyMode) {
        analytics.logEvent(
            event = NEW_SEND_SWITCH_CURRENCY_MODE_CLICK,
            params = mapOf(
                "Crypto" to (mode is CurrencyMode.Token)
            )
        )
    }

    fun logSendConfirmButtonClicked(
        tokenName: String,
        amountInToken: String,
        amountInUsd: String,
        isFeeFree: Boolean,
        mode: CurrencyMode
    ) {
        analytics.logEvent(
            event = NEW_SEND_CONFIRM_BUTTON_CLICK,
            params = mapOf(
                "Token" to tokenName,
                "Max" to isMaxButtonClicked,
                "Amount_Token" to amountInToken,
                "Amount_USD" to amountInUsd,
                "Fee" to isFeeFree,
                "Fiat_Input" to (mode is CurrencyMode.Fiat)
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
        BITCOIN("Bitcoin")
    }

    private fun NetworkType.toAnalyticsValue(): AnalyticsSendNetwork = when (this) {
        NetworkType.SOLANA -> AnalyticsSendNetwork.SOLANA
        NetworkType.BITCOIN -> AnalyticsSendNetwork.BITCOIN
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
