package org.p2p.wallet.newsend.analytics

import org.p2p.wallet.common.analytics.Analytics
import org.p2p.wallet.common.analytics.constants.EventNames.NEW_SEND_CONFIRM_BUTTON_CLICK
import org.p2p.wallet.common.analytics.constants.EventNames.NEW_SEND_FREE_TRANSACTIONS_CLICK
import org.p2p.wallet.common.analytics.constants.EventNames.NEW_SEND_RECIPIENT_ADD
import org.p2p.wallet.common.analytics.constants.EventNames.NEW_SEND_RECIPIENT_VIEWED
import org.p2p.wallet.common.analytics.constants.EventNames.NEW_SEND_SWITCH_CURRENCY_MODE_CLICK
import org.p2p.wallet.common.analytics.constants.EventNames.NEW_SEND_TOKEN_SELECTION_CLICK
import org.p2p.wallet.common.analytics.constants.EventNames.NEW_SEND_VIEWED
import org.p2p.wallet.send.model.CurrencyMode

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
                "Fiat_Input" to (mode is CurrencyMode.Token)
            )
        )
    }

    fun logNewSearchScreenOpened() {
        analytics.logEvent(event = NEW_SEND_RECIPIENT_VIEWED)
    }

    fun logRecipientSelected(recipient: String, type: RecipientSelectionType) {
        analytics.logEvent(
            event = NEW_SEND_RECIPIENT_ADD,
            params = mapOf(
                "Recipient_With" to recipient,
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
