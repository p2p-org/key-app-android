package org.p2p.wallet.jupiter.analytics

import org.p2p.wallet.common.analytics.Analytics
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRoute
import org.p2p.wallet.jupiter.ui.info.SwapInfoType
import org.p2p.wallet.swap.model.PERCENT_DIVIDE_VALUE
import org.p2p.wallet.swap.model.Slippage

private const val SWAP_SETTINGS_FEE_CLICK = "Swap_Settings_Fee_Click"
private const val SWAP_SETTINGS_SLIPPAGE = "Swap_Settings_Slippage"
private const val SWAP_SETTINGS_SLIPPAGE_CUSTOM = "Swap_Settings_Slippage_Custom"
private const val SWAP_SETTINGS_SWAPPING_THROUGH_CHOICE = "Swap_Settings_Swapping_Through_Choice"

class JupiterSwapSettingsAnalytics(private val tracker: Analytics) {
    fun logFeeDetailsClicked(feeType: SwapInfoType) {
        tracker.logEvent(
            event = SWAP_SETTINGS_FEE_CLICK,
            params = mapOf("Fee_Name" to feeType.toAnalyticsValue().value)
        )
    }

    fun logChangeRouteClicked() {
        tracker.logEvent(
            event = SWAP_SETTINGS_FEE_CLICK,
            params = mapOf("Fee_Name" to SwapFeeTypeAnalytics.ROUTE_SELECT.value)
        )
    }

    fun logSlippageChangedClicked(newValue: Slippage) {
        val eventName = when (newValue) {
            is Slippage.Custom -> SWAP_SETTINGS_SLIPPAGE_CUSTOM
            else -> SWAP_SETTINGS_SLIPPAGE
        }
        tracker.logEvent(
            event = eventName,
            params = mapOf("Slippage_Level_Percent" to newValue.doubleValue * PERCENT_DIVIDE_VALUE)
        )
    }

    fun logSwapRouteChanged(route: JupiterSwapRoute) {
        tracker.logEvent(
            event = SWAP_SETTINGS_SWAPPING_THROUGH_CHOICE,
            params = mapOf("Variant" to formatRouteName(route))
        )
    }

    private fun formatRouteName(route: JupiterSwapRoute): String = buildString {
        route.marketInfos.forEachIndexed { index, marketInfo ->
            append(marketInfo.label)
            if (index != route.marketInfos.lastIndex) append("_")
        }
    }

    private fun SwapInfoType.toAnalyticsValue(): SwapFeeTypeAnalytics {
        return when (this) {
            SwapInfoType.NETWORK_FEE -> SwapFeeTypeAnalytics.NETWORK_FEE
            SwapInfoType.ACCOUNT_FEE -> SwapFeeTypeAnalytics.SOL_ACCOUNT_CREATION
            SwapInfoType.LIQUIDITY_FEE -> SwapFeeTypeAnalytics.LIQUIDITY_FEE
            SwapInfoType.MINIMUM_RECEIVED -> SwapFeeTypeAnalytics.ESTIMATED_FEES
        }
    }

    enum class SwapFeeTypeAnalytics(val value: String) {
        ROUTE_SELECT("Swapping_Through"),
        NETWORK_FEE("Network_Fee"),
        SOL_ACCOUNT_CREATION("Sol_Account_Creation"),
        LIQUIDITY_FEE("Liquidity_Fee"),
        ESTIMATED_FEES("Estimated_Fees")
    }
}
