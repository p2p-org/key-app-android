package org.p2p.wallet.swap.analytics

import org.p2p.wallet.auth.analytics.AuthAnalytics
import org.p2p.wallet.common.analytics.Analytics
import org.p2p.wallet.common.analytics.constants.EventNames.SWAP_CHANGING_CURRENCY
import org.p2p.wallet.common.analytics.constants.EventNames.SWAP_CHANGING_TOKEN_A
import org.p2p.wallet.common.analytics.constants.EventNames.SWAP_CHANGING_TOKEN_B
import org.p2p.wallet.common.analytics.constants.EventNames.SWAP_COMPLETED
import org.p2p.wallet.common.analytics.constants.EventNames.SWAP_CREATING_ANOTHER
import org.p2p.wallet.common.analytics.constants.EventNames.SWAP_GOING_BACK
import org.p2p.wallet.common.analytics.constants.EventNames.SWAP_PROCESS_SHOWN
import org.p2p.wallet.common.analytics.constants.EventNames.SWAP_REVERSING
import org.p2p.wallet.common.analytics.constants.EventNames.SWAP_REVIEWING
import org.p2p.wallet.common.analytics.constants.EventNames.SWAP_REVIEWING_HELP_CLOSED
import org.p2p.wallet.common.analytics.constants.EventNames.SWAP_SETTING_SETTINGS
import org.p2p.wallet.common.analytics.constants.EventNames.SWAP_SHOWING_DETAILS
import org.p2p.wallet.common.analytics.constants.EventNames.SWAP_SHOWING_HISTORY
import org.p2p.wallet.common.analytics.constants.EventNames.SWAP_SHOWING_SETTINGS
import org.p2p.wallet.common.analytics.constants.EventNames.SWAP_SHOW_DETAILS_PRESSED
import org.p2p.wallet.common.analytics.constants.EventNames.SWAP_STARTED
import org.p2p.wallet.common.analytics.constants.EventNames.SWAP_USER_CONFIRMED
import org.p2p.wallet.common.analytics.constants.EventNames.SWAP_VERIFICATION_INVOKED
import org.p2p.wallet.common.analytics.constants.EventNames.SWAP_VIEWED
import java.math.BigDecimal

class SwapAnalytics(private val trackerContract: Analytics) {

    fun logSwapViewed(lastScreenName: String) {
        trackerContract.logEvent(
            SWAP_VIEWED,
            arrayOf(
                Pair("Last_Screen", lastScreenName)
            )
        )
    }

    fun logSwapChangingTokenA(tokenName: String) {
        trackerContract.logEvent(
            SWAP_CHANGING_TOKEN_A,
            arrayOf(
                Pair("Token_A_Name", tokenName)
            )
        )
    }

    fun logSwapChangingTokenB(tokenName: String) {
        trackerContract.logEvent(
            SWAP_CHANGING_TOKEN_B,
            arrayOf(
                Pair("Token_B_Name", tokenName)
            )
        )
    }

    fun logSwapReversing(reverseTokenName: String) {
        trackerContract.logEvent(
            SWAP_REVERSING,
            arrayOf(
                Pair("Token_B_Name", reverseTokenName)
            )
        )
    }

    fun logSwapShowingSettings(
        priceSlippage: Double,
        priceSlippageExact: Boolean,
        feesSource: FeeSource,
        swapSettingsSource: SwapSettingsSource
    ) {
        trackerContract.logEvent(
            SWAP_SHOWING_SETTINGS,
            arrayOf(
                Pair("Price_Slippage", priceSlippage),
                Pair("Price_Slippage_Exact", priceSlippageExact),
                Pair("Fees_Source", feesSource.title),
                Pair("Swap_Settings_Source", swapSettingsSource.title)
            )
        )
    }

    fun logSwapSettingSettings(
        priceSlippage: Double,
        priceSlippageExact: Boolean,
        feesSource: FeeSource
    ) {
        trackerContract.logEvent(
            SWAP_SETTING_SETTINGS,
            arrayOf(
                Pair("Price_Slippage", priceSlippage),
                Pair("Price_Slippage_Exact", priceSlippageExact),
                Pair("Fees_Source", feesSource.title)
            )
        )
    }

    fun logSwapChangingCurrency(currency: String) {
        trackerContract.logEvent(
            SWAP_CHANGING_CURRENCY,
            arrayOf(
                Pair("Swap_Currency", currency)
            )
        )
    }

    fun logSwapShowDetailsPressed() {
        trackerContract.logEvent(SWAP_SHOW_DETAILS_PRESSED)
    }

    fun logSwapGoingBack(
        tokenAName: String,
        tokenBName: String,
        swapCurrency: String,
        swapSum: BigDecimal,
        swapMax: Boolean,
        swapUSD: BigDecimal,
        priceSlippage: Double,
        priceSlippageExact: Boolean,
        feesSource: FeeSource
    ) {
        trackerContract.logEvent(
            SWAP_GOING_BACK,
            arrayOf(
                Pair("Token_A_Name", tokenAName),
                Pair("Token_B_Name", tokenBName),
                Pair("Swap_Currency", swapCurrency),
                Pair("Swap_Sum", swapSum),
                Pair("Swap_MAX", swapMax),
                Pair("Swap_USD", swapUSD),
                Pair("Price_Slippage", priceSlippage),
                Pair("PriceSlippage_Exact", priceSlippageExact),
                Pair("Fees_Source", feesSource.title)
            )
        )
    }

    fun logSwapReviewing(
        tokenAName: String,
        tokenBName: String,
        swapCurrency: String,
        swapSum: BigDecimal,
        swapMax: Boolean,
        swapUSD: BigDecimal,
        priceSlippage: Int,
        priceSlippageExact: Boolean,
        feesSource: FeeSource
    ) {
        trackerContract.logEvent(
            SWAP_REVIEWING,
            arrayOf(
                Pair("Token_A_Name", tokenAName),
                Pair("Token_B_Name", tokenBName),
                Pair("Swap_Currency", swapCurrency),
                Pair("Swap_Sum", swapSum),
                Pair("Swap_MAX", swapMax),
                Pair("Swap_USD", swapUSD),
                Pair("Price_Slippage", priceSlippage),
                Pair("PriceSlippage_Exact", priceSlippageExact),
                Pair("Fees_Source", feesSource.title)
            )
        )
    }

    fun logSwapReviewingHelpClosed() {
        trackerContract.logEvent(SWAP_REVIEWING_HELP_CLOSED)
    }

    fun logSwapVerificationInvoked(authType: AuthAnalytics.AuthType) {
        trackerContract.logEvent(
            SWAP_VERIFICATION_INVOKED,
            arrayOf(
                Pair("Auth_Type", authType.title)
            )
        )
    }

    fun logSwapProcessShown() {
        trackerContract.logEvent(SWAP_PROCESS_SHOWN)
    }

    fun logSwapCreatingAnother(swapStatus: SwapStatus) {
        trackerContract.logEvent(
            SWAP_CREATING_ANOTHER,
            arrayOf(
                Pair("Swap_Status", swapStatus.title)
            )
        )
    }

    fun logSwapShowingHistory() {
        trackerContract.logEvent(SWAP_SHOWING_HISTORY)
    }

    fun logSwapShowingDetails(
        swapStatus: SwapStatus,
        lastScreenName: String,
        tokenAName: String,
        tokenBName: String,
        swapSum: BigDecimal,
        swapUSD: BigDecimal,
        feesSource: FeeSource
    ) {

        trackerContract.logEvent(
            SWAP_SHOWING_DETAILS,
            arrayOf(
                Pair("Swap_Status", swapStatus.title),
                Pair("Last_Screen", lastScreenName),
                Pair("Token_A_Name", tokenAName),
                Pair("Token_B_Name", tokenBName),
                Pair("Swap_Sum", swapSum),
                Pair("Swap_USD", swapUSD),
                Pair("Fees_Source", feesSource.title)
            )
        )
    }

    fun logSwapUserConfirmed(
        tokenAName: String,
        tokenBName: String,
        swapSum: String,
        isSwapMax: Boolean,
        swapUsd: BigDecimal,
        priceSlippage: Double,
        feesSource: FeeSource
    ) {
        trackerContract.logEvent(
            event = SWAP_USER_CONFIRMED,
            params = mapOf(
                "Token_A_Name" to tokenAName,
                "Token_B_Name" to tokenBName,
                "Swap_Sum" to swapSum,
                "Swap_MAX" to if (isSwapMax) "True" else "False",
                "Swap_USD" to swapUsd.toString(),
                "Price_Slippage" to priceSlippage.toString(),
                "Fees_Source" to feesSource.title
            )
        )
    }

    fun logSwapStarted(
        tokenAName: String,
        tokenBName: String,
        swapSum: String,
        isSwapMax: Boolean,
        swapUsd: BigDecimal,
        priceSlippage: Double,
        feesSource: FeeSource
    ) {
        trackerContract.logEvent(
            event = SWAP_STARTED,
            params = mapOf(
                "Token_A_Name" to tokenAName,
                "Token_B_Name" to tokenBName,
                "Swap_Sum" to swapSum,
                "Swap_MAX" to if (isSwapMax) "True" else "False",
                "Swap_USD" to swapUsd.toString(),
                "Price_Slippage" to priceSlippage.toString(),
                "Fees_Source" to feesSource.title
            )
        )
    }

    fun logSwapCompleted(
        tokenAName: String,
        tokenBName: String,
        swapSum: String,
        isSwapMax: Boolean,
        swapUsd: BigDecimal,
        priceSlippage: Double,
        feesSource: FeeSource
    ) {
        trackerContract.logEvent(
            event = SWAP_COMPLETED,
            params = mapOf(
                "Token_A_Name" to tokenAName,
                "Token_B_Name" to tokenBName,
                "Swap_Sum" to swapSum,
                "Swap_MAX" to if (isSwapMax) "True" else "False",
                "Swap_USD" to swapUsd.toString(),
                "Price_Slippage" to priceSlippage,
                "Fees_Source" to feesSource.title
            )
        )
    }

    enum class SwapStatus(val title: String) {
        SUCCESS("Success"),
        PENDING("Pending"),
        ERROR("Error")
    }

    enum class FeeSource(val title: String) {
        SOL("SOL"),
        UNKNOWN("Unknown"),
        OTHER("Other");

        companion object {
            fun getValueOf(tokenSymbol: String): FeeSource = if (tokenSymbol == "SOL") SOL else OTHER
        }
    }

    enum class SwapSettingsSource(val title: String) {
        ICON("Icon"),
        SLIPPAGE("Slippage"),
        FEES("Fees")
    }
}
