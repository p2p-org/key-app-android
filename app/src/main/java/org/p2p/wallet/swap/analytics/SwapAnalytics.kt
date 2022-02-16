package org.p2p.wallet.swap.analytics

import org.p2p.wallet.auth.analytics.AuthAnalytics
import org.p2p.wallet.common.analytics.TrackerContract
import java.math.BigDecimal

class SwapAnalytics(private val trackerContract: TrackerContract) {

    fun logSwapViewed(lastScreenName: String) {
        trackerContract.logEvent(
            "Swap_Viewed",
            arrayOf(
                Pair("Last_Screen", lastScreenName)
            )
        )
    }

    fun logSwapChangingTokenA(tokenName: String) {
        trackerContract.logEvent(
            "Swap_Changing_Token_A",
            arrayOf(
                Pair("Token_A_Name", tokenName)
            )
        )
    }

    fun logSwapChangingTokenB(tokenName: String) {
        trackerContract.logEvent(
            "Swap_Changing_Token_B",
            arrayOf(
                Pair("Token_B_Name", tokenName)
            )
        )
    }

    fun logSwapReversing(reverseTokenName: String) {
        trackerContract.logEvent(
            "Swap_Reversing",
            arrayOf(
                Pair("Token_B_Name", reverseTokenName)
            )
        )
    }

    fun logSwapShowingSettings(
        priceSlippage: Int,
        priceSlippageExact: Boolean,
        feesSource: FeeSource,
        swapSettingsSource: SwapSettingsSource
    ) {
        trackerContract.logEvent(
            "Swap_Showing_Settings",
            arrayOf(
                Pair("Price_Slippage", priceSlippage),
                Pair("Price_Slippage_Exact", priceSlippageExact),
                Pair("Fees_Source", feesSource.title),
                Pair("Swap_Settings_Source", swapSettingsSource.title)
            )
        )
    }

    fun logSwapSettingSettings(
        priceSlippage: Int,
        priceSlippageExact: Boolean,
        feesSource: FeeSource
    ) {
        trackerContract.logEvent(
            "Swap_Setting_Settings",
            arrayOf(
                Pair("Price_Slippage", priceSlippage),
                Pair("Price_Slippage_Exact", priceSlippageExact),
                Pair("Fees_Source", feesSource.title)
            )
        )
    }

    fun logSwapChangingCurrency(currency: String) {
        trackerContract.logEvent(
            "Swap_Changing_Currency",
            arrayOf(
                Pair("Swap_Currency", currency)
            )
        )
    }

    fun logSwapShowDetailsPressed() {
        trackerContract.logEvent("Swap_Show_Details_Pressed")
    }

    fun logSwapGoingBack(
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
            "Swap_Going_Back",
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
            "Swap_Reviewing",
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
        trackerContract.logEvent("Swap_Reviewing_Help_Closed")
    }

    fun logSwapVerificationInvoked(authType: AuthAnalytics.AuthType) {
        trackerContract.logEvent(
            "Swap_Verification_Invoked",
            arrayOf(
                Pair("Auth_Type", authType.title)
            )
        )
    }

    fun logSwapProcessShown() {
        trackerContract.logEvent("Swap_Process_Shown")
    }

    fun logSwapCreatingAnother(swapStatus: SwapStatus) {
        trackerContract.logEvent(
            "Swap_Creating_Another",
            arrayOf(
                Pair("Swap_Status", swapStatus.title)
            )
        )
    }

    fun logSwapShowingHistory() {
        trackerContract.logEvent("Swap_Showing_History")
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
            "Swap_Showing_Details",
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
            fun getValueOf(tokenSymbol: String): FeeSource {
                return if (tokenSymbol == "SOL") SOL else OTHER
            }
        }
    }

    enum class SwapSettingsSource(val title: String) {
        ICON("Icon"),
        SLIPPAGE("Slippage"),
        FEES("Fees")
    }
}