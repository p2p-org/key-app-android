package org.p2p.wallet.bridge.analytics

import org.p2p.wallet.common.analytics.Analytics

private const val SEND_BRIDGES_SCREEN_OPEN = "Send_Bridges_Screen_Open"

private const val NEW_SEND_FREE_TRANSACTIONS_CLICK = "Sendnew_Free_Transaction_Click"
private const val NEW_SEND_TOKEN_SELECTION_CLICK = "Sendnew_Token_Input_Click"
private const val SEND_CLICK_CHANGE_TOKEN_CHOSEN = "Send_Click_Change_Token_Chosen"
private const val SEND_CLICK_CHANGE_TOKEN_VALUE = "Send_Click_Change_Token_Value"
private const val SEND_BRIDGES_CONFIRM_BUTTON_CLICK = "Send_Bridges_Confirm_Button_Click"

class SendBridgesAnalytics(
    private val analytics: Analytics
) {

    private val bridgeSendFlow = "Send_Flow" to "Bridge"
    private val bridgesParams = mapOf(bridgeSendFlow)

    fun logSendBridgesScreenOpened() {
        analytics.logEvent(event = SEND_BRIDGES_SCREEN_OPEN)
    }

    fun logFreeTransactionsClicked() {
        analytics.logEvent(event = NEW_SEND_FREE_TRANSACTIONS_CLICK, bridgesParams)
    }

    fun logTokenSelectionClicked() {
        analytics.logEvent(event = NEW_SEND_TOKEN_SELECTION_CLICK, bridgesParams)
    }

    fun logTokenChanged(newTokenSymbol: String) {
        analytics.logEvent(
            event = SEND_CLICK_CHANGE_TOKEN_CHOSEN,
            params = mapOf(
                "Token_Name" to newTokenSymbol,
                bridgeSendFlow
            )
        )
    }

    fun logTokenAmountChanged(tokenSymbol: String, formattedNewAmount: String) {
        analytics.logEvent(
            event = SEND_CLICK_CHANGE_TOKEN_VALUE,
            params = mapOf(
                "Token_Name" to tokenSymbol,
                "Token_Value" to formattedNewAmount,
                bridgeSendFlow
            )
        )
    }

    fun logSendConfirmButtonClicked(
        tokenSymbol: String,
        amountInToken: String,
        amountInUsd: String,
        fee: String,
    ) {
        analytics.logEvent(
            event = SEND_BRIDGES_CONFIRM_BUTTON_CLICK,
            params = mapOf(
                "Token_Name" to tokenSymbol,
                "Token_Value" to amountInToken,
                "Value_Fiat" to amountInUsd,
                "Fee" to fee,
            )
        )
    }
}
