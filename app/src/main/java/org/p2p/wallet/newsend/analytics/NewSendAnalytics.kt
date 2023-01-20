package org.p2p.wallet.newsend.analytics

import org.p2p.core.model.CurrencyMode
import org.p2p.wallet.common.analytics.Analytics
import org.p2p.wallet.send.model.SearchResult

const val NEW_SEND_RECIPIENT_VIEWED = "Sendnew_Recipient_Screen"
const val NEW_SEND_RECIPIENT_ADD = "Sendnew_Recipient_Add"
const val NEW_SEND_VIEWED = "Sendnew_Input_Screen"
const val NEW_SEND_FREE_TRANSACTIONS_CLICK = "Sendnew_Free_Transaction_Click"
const val NEW_SEND_TOKEN_SELECTION_CLICK = "Sendnew_Token_Input_Click"
const val NEW_SEND_SWITCH_CURRENCY_MODE_CLICK = "Sendnew_Fiat_Input_Click"
const val NEW_SEND_CONFIRM_BUTTON_CLICK = "Sendnew_Confirm_Button_Click"

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

    enum class RecipientSelectionType(val type: String) {
        RECENT("Recent"),
        SEARCH("Search")
    }
}
