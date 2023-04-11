package org.p2p.wallet.bridge.anatytics

import java.math.BigDecimal
import org.p2p.wallet.common.analytics.Analytics

private const val CLAIM_BRIDGES_SCREEN_OPEN = "Claim_Bridges_Screen_Open"

private const val CLAIM_BRIDGES_BUTTON_CLICK = "Claim_Bridges_Button_Click"
private const val CLAIM_BRIDGES_FEE_CLICK = "Claim_Bridges_Fee_Click"
private const val CLAIM_BRIDGES_CLICK_CONFIRMED = "Claim_Bridges_Click_Confirmed"

class ClaimAnalytics(
    private val analytics: Analytics
) {

    fun logScreenOpened() {
        analytics.logEvent(event = CLAIM_BRIDGES_SCREEN_OPEN)
    }

    fun logClaimButtonClicked() {
        analytics.logEvent(event = CLAIM_BRIDGES_BUTTON_CLICK)
    }

    fun logFeesButtonClicked() {
        analytics.logEvent(event = CLAIM_BRIDGES_FEE_CLICK)
    }

    fun logConfirmClaimButtonClicked(
        tokenSymbol: String,
        tokenAmount: BigDecimal,
        tokenAmountInFiat: BigDecimal,
        isFree: Boolean,
    ) {
        analytics.logEvent(
            event = CLAIM_BRIDGES_CLICK_CONFIRMED,
            params = mapOf(
                "Token_Name" to tokenSymbol,
                "Token_Value" to tokenAmount,
                "Value_Fiat" to tokenAmountInFiat,
                "Free" to isFree,
            )
        )
    }
}
