package org.p2p.wallet.svl.analytics

import org.p2p.wallet.common.analytics.Analytics
import org.p2p.wallet.newsend.model.TemporaryAccount

private const val SEND_CLICK_START_CREATE_LINK = "Send_Click_Start_Create_Link"
private const val SEND_CLICK_NOTIFICATION_FREE_TRANSACTIONS = "Send_Click_Notification_Free_Transactions"
private const val SEND_CLICK_CHANGE_TOKEN = "Send_Click_Change_Token"
private const val SEND_CLICK_CHANGE_TOKEN_CHOSEN = "Send_Click_Change_Token_Chosen"
private const val SEND_CLICK_CHANGE_TOKEN_VALUE = "Send_Click_Change_Token_Value"
private const val SEND_CLICK_CREATE_LINK = "Send_Click_Create_Link"
private const val SEND_CREATING_LINK_PROCESS_SCREEN_OPEN = "Send_Creating_Link_Process_Screen_Open"
private const val SEND_CREATING_LINK_END_SCREEN_OPEN = "Send_Creating_Link_End_Screen_Open"
private const val SEND_CLICK_SHARE_LINK = "Send_Click_Share_Link"
private const val SEND_CLICK_COPY_LINK = "Send_Click_Copy_Link"
private const val SEND_CLICK_DEFAULT_ERROR = "Send_Click_Default_Error"

class SendViaLinkAnalytics(
    private val tracker: Analytics
) {

    fun logStartCreateLink() {
        tracker.logEvent(SEND_CLICK_START_CREATE_LINK)
    }

    fun logFreeTransactionsClicked() {
        tracker.logEvent(SEND_CLICK_NOTIFICATION_FREE_TRANSACTIONS)
    }

    fun logTokenChangeClicked(currentTokenSymbol: String) {
        tracker.logEvent(
            event = SEND_CLICK_CHANGE_TOKEN,
            params = mapOf("Token_Name" to currentTokenSymbol)
        )
    }

    fun logTokenChanged(newTokenSymbol: String) {
        tracker.logEvent(
            event = SEND_CLICK_CHANGE_TOKEN_CHOSEN,
            params = mapOf("Token_Name" to newTokenSymbol)
        )
    }

    fun logTokenAmountChanged(tokenSymbol: String, formattedNewAmount: String) {
        tracker.logEvent(
            event = SEND_CLICK_CHANGE_TOKEN_VALUE,
            params = mapOf(
                "Token_Name" to tokenSymbol,
                "Token_Value" to formattedNewAmount
            )
        )
    }

    fun logCreateLinkClicked(tokenSymbol: String, formattedAmount: String, temporaryAccount: TemporaryAccount) {
        tracker.logEvent(
            event = SEND_CLICK_CREATE_LINK,
            params = mapOf(
                "Token_Name" to tokenSymbol,
                "Token_Value" to formattedAmount,
                "PubKey" to temporaryAccount.publicKey.toBase58()
            )
        )
    }

    fun logLinkGenerationOpened() {
        tracker.logEvent(SEND_CREATING_LINK_PROCESS_SCREEN_OPEN)
    }

    fun logLinkGeneratedSuccessOpened(tokenSymbol: String, tokenAmount: String, temporaryAccountPublicKey: String) {
        tracker.logEvent(
            event = SEND_CREATING_LINK_END_SCREEN_OPEN,
            params = mapOf(
                "Token_Name" to tokenSymbol,
                "Token_Value" to tokenAmount,
                "PubKey" to temporaryAccountPublicKey
            )
        )
    }

    fun logLinkGeneratedErrorOpened() {
        tracker.logEvent(SEND_CLICK_DEFAULT_ERROR)
    }

    fun logLinkShareButtonClicked() {
        tracker.logEvent(SEND_CLICK_SHARE_LINK)
    }

    fun logLinkCopyIconClicked() {
        tracker.logEvent(SEND_CLICK_COPY_LINK)
    }
}
