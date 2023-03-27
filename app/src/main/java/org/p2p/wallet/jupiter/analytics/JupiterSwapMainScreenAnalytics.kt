package org.p2p.wallet.jupiter.analytics

import java.math.BigDecimal
import org.p2p.wallet.common.analytics.Analytics
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.jupiter.ui.tokens.SwapTokensListMode
import org.p2p.wallet.swap.ui.orca.SwapOpenedFrom

private const val SWAP_START_SCREEN = "Swap_Start_Screen"
private const val SWAP_CHANGING_TOKEN_A_CLICK = "Swap_Changing_Token_A_Click"
private const val SWAP_CHANGING_TOKEN_B_CLICK = "Swap_Changing_Token_B_Click"
private const val SWAP_CHANGING_VALUE_TOKEN_A = "Swap_Changing_Value_Token_A"
private const val SWAP_CHANGING_VALUE_TOKEN_B = "Swap_Changing_Value_Token_B"
private const val SWAP_CHANGING_VALUE_TOKEN_A_ALL = "Swap_Changing_Value_Token_A_All"
private const val SWAP_SWITCH_TOKENS = "Swap_Switch_Tokens"
private const val SWAP_PRICE_IMPACT_LOW = "Swap_Price_Impact_Low"
private const val SWAP_PRICE_IMPACT_HIGH = "Swap_Price_Impact_High"
private const val SWAP_SETTINGS_CLICK = "Swap_Settings_Click"
private const val SWAP_CLICK_APPROVE_BUTTON = "Swap_Click_Approve_Button_New"
private const val SWAP_ERROR_TOKEN_A_INSUFFICIENT_AMOUNT = "Swap_Error_Token_A_Insufficient_Amount"
private const val SWAP_ERROR_TOKEN_A_MIN = "Swap_Error_Token_A_Min"
private const val SWAP_ERROR_TOKEN_PAIR_NOT_EXIST = "Swap_Error_Token_Pair_Not_Exist"

private const val SWAP_CHANGING_TOKEN_A = "Swap_Changing_Token_A"
private const val SWAP_RETURN_FROM_CHANGING_TOKEN_A = "Swap_Return_From_Changing_Token_A"
private const val SWAP_CHANGING_TOKEN_B = "Swap_Changing_Token_B"
private const val SWAP_RETURN_FROM_CHANGING_TOKEN_B = "Swap_Return_From_Changing_Token_B"

class JupiterSwapMainScreenAnalytics(private val tracker: Analytics) {
    fun logStartScreen(
        openedFrom: SwapOpenedFrom,
        initialTokenA: SwapTokenModel,
        initialTokenB: SwapTokenModel
    ) {
        tracker.logEvent(
            event = SWAP_START_SCREEN,
            params = mapOf(
                "Last_Screen" to openedFrom.toAnalyticsValue().value,
                "From" to initialTokenA.tokenSymbol,
                "To" to initialTokenB.tokenSymbol
            )
        )
    }

    fun logChangeTokenA(tokenA: SwapTokenModel) {
        tracker.logEvent(
            event = SWAP_CHANGING_TOKEN_A_CLICK,
            params = mapOf("Token_A_Name" to tokenA.tokenSymbol)
        )
    }

    fun logChangeTokenB(tokenB: SwapTokenModel) {
        tracker.logEvent(
            event = SWAP_CHANGING_TOKEN_B_CLICK,
            params = mapOf("Token_B_Name" to tokenB.tokenSymbol)
        )
    }

    fun logChangeTokenAAmountChanged(tokenA: SwapTokenModel, newAmount: String) {
        tracker.logEvent(
            event = SWAP_CHANGING_VALUE_TOKEN_A,
            params = mapOf(
                "Token_A_Name" to tokenA.tokenSymbol,
                "Token_A_Value" to newAmount
            )
        )
    }

    fun logChangeTokenBAmountChanged(tokenB: SwapTokenModel, newAmount: String) {
        tracker.logEvent(
            event = SWAP_CHANGING_VALUE_TOKEN_B,
            params = mapOf(
                "Token_B_Name" to tokenB.tokenSymbol,
                "Token_B_Value" to newAmount
            )
        )
    }

    fun logTokenAAllClicked(tokenAName: String, tokenAAmount: String) {
        tracker.logEvent(
            event = SWAP_CHANGING_VALUE_TOKEN_A_ALL,
            params = mapOf(
                "Token_A_Name" to tokenAName,
                "Token_A_Value" to tokenAAmount
            )
        )
    }

    fun logTokensSwitchClicked(newTokenA: SwapTokenModel, newTokenB: SwapTokenModel) {
        tracker.logEvent(
            event = SWAP_SWITCH_TOKENS,
            params = mapOf(
                "Token_A_Name" to newTokenA.tokenSymbol,
                "Token_B_Name" to newTokenB.tokenSymbol
            )
        )
    }

    fun logPriceImpactLow(priceImpact: BigDecimal) {
        tracker.logEvent(
            event = SWAP_PRICE_IMPACT_LOW,
            params = mapOf("Price_Impact" to priceImpact.toString())
        )
    }

    fun logPriceImpactHigh(priceImpact: BigDecimal) {
        tracker.logEvent(
            event = SWAP_PRICE_IMPACT_HIGH,
            params = mapOf("Price_Impact" to priceImpact.toString())
        )
    }

    fun logSwapSettingsClicked() {
        tracker.logEvent(event = SWAP_SETTINGS_CLICK)
    }

    fun logApproveSwapClicked(
        tokenA: SwapTokenModel,
        tokenB: SwapTokenModel,
        tokenAAmount: String,
        tokenBAmountUsd: String,
        signature: String
    ) {
        tracker.logEvent(
            event = SWAP_CLICK_APPROVE_BUTTON,
            params = mapOf(
                "Token_A" to tokenA.tokenSymbol,
                "Token_B" to tokenB.tokenSymbol,
                "Swap_Sum" to tokenAAmount,
                "Swap_USD" to tokenBAmountUsd,
                "Signature" to signature
            )
        )
    }

    fun logNotEnoughTokenA() {
        tracker.logEvent(event = SWAP_ERROR_TOKEN_A_INSUFFICIENT_AMOUNT)
    }

    fun logTooSmallTokenA() {
        tracker.logEvent(event = SWAP_ERROR_TOKEN_A_MIN)
    }

    fun logSwapPairNotExists() {
        tracker.logEvent(event = SWAP_ERROR_TOKEN_PAIR_NOT_EXIST)
    }

    fun logTokenChanged(listMode: SwapTokensListMode, token: SwapTokenModel) {
        val eventName: String
        val params: Map<String, Any>
        if (listMode == SwapTokensListMode.TOKEN_A) {
            eventName = SWAP_CHANGING_TOKEN_A
            params = mapOf(
                "Token_A_Name" to token.tokenSymbol,
                "Token_A_Value" to if (token is SwapTokenModel.UserToken) token.tokenAmount else 0
            )
        } else {
            eventName = SWAP_CHANGING_TOKEN_B
            params = mapOf(
                "Token_B_Name" to token.tokenSymbol,
                "Token_B_Value" to if (token is SwapTokenModel.UserToken) token.tokenAmount else 0
            )
        }
        tracker.logEvent(eventName, params)
    }

    fun logSelectTokenClosed(listMode: SwapTokensListMode) {
        val eventName = if (listMode == SwapTokensListMode.TOKEN_A) {
            SWAP_RETURN_FROM_CHANGING_TOKEN_A
        } else {
            SWAP_RETURN_FROM_CHANGING_TOKEN_B
        }
        tracker.logEvent(eventName)
    }

    private enum class SwapOpenedFromAnalytics(val value: String) {
        ACTION_PANEL("Action_Panel"),
        MAIN_SCREEN("Tap_Main"),
        TOKEN_SCREEN("Tap_Token"),
    }

    private fun SwapOpenedFrom.toAnalyticsValue(): SwapOpenedFromAnalytics {
        return when (this) {
            SwapOpenedFrom.ACTION_PANEL -> SwapOpenedFromAnalytics.ACTION_PANEL
            SwapOpenedFrom.TOKEN_SCREEN -> SwapOpenedFromAnalytics.TOKEN_SCREEN
            SwapOpenedFrom.MAIN_SCREEN, SwapOpenedFrom.BOTTOM_NAVIGATION -> SwapOpenedFromAnalytics.MAIN_SCREEN
        }
    }
}
