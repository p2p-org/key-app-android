package org.p2p.wallet.svl.ui.send

import org.p2p.wallet.common.analytics.Analytics
import org.p2p.wallet.newsend.model.TemporaryAccount

private const val CLAIM_START_SCREEN_OPEN = "Claim_Start_Screen_Open"
private const val CLAIM_CLICK_CONFIRMED = "Claim_Click_Confirmed"
private const val CLAIM_CLICK_CLOSE = "Claim_Click_Close"
private const val CLAIM_END_SCREEN_OPEN = "Claim_End_Screen_Open"
private const val CLAIM_CLICK_END = "Claim_Click_End"
private const val CLAIM_ERROR_ALREADY_CLAIMED = "Claim_Error_Already_Claimed"
private const val CLAIM_ERROR_DEFAULT_REJECT = "Claim_Error_Default_Reject"

class SvlReceiveFundsAnalytics(private val tracker: Analytics) {

    fun logClaimStartedOpened() {
        tracker.logEvent(CLAIM_START_SCREEN_OPEN)
    }

    fun logClaimConfirmStarted(
        temporaryAccount: TemporaryAccount,
        tokenSymbol: String,
        tokenAmount: String,
        linkAuthor: String
    ) {
        tracker.logEvent(
            event = CLAIM_CLICK_CONFIRMED,
            params = mapOf(
                "PubKey" to temporaryAccount.publicKey.toBase58(),
                "Token_Name" to tokenSymbol,
                "Token_Value" to tokenAmount,
                "From_Address" to linkAuthor
            )
        )
    }

    fun logCloseClicked() {
        tracker.logEvent(CLAIM_CLICK_CLOSE)
    }

    fun logClaimSuccess() {
        tracker.logEvent(CLAIM_END_SCREEN_OPEN)
    }

    fun logClaimGotItClicked() {
        tracker.logEvent(CLAIM_CLICK_END)
    }

    fun logClaimAlreadyClaimed() {
        tracker.logEvent(CLAIM_ERROR_ALREADY_CLAIMED)
    }

    fun logClaimFailed() {
        tracker.logEvent(CLAIM_ERROR_DEFAULT_REJECT)
    }
}
