package org.p2p.wallet.swap.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/*
* We are sending to blockchain converting slippage to coersing at most 1 value
* Since 100 percent is slippage 1
* We are dividing every custom slippage value to 100
* Thus:
* 1 percent is 0.01 value
* 5 percent is 0.05 value
* 10 percent is 0.1 value
* 50 percent is 0.5 value
* etc.
* */
const val PERCENT_DIVIDE_VALUE = 100
const val MAX_ALLOWED_SLIPPAGE = 50

sealed class Slippage(val doubleValue: Double, val percentValue: String) : Parcelable {
    @Parcelize
    object Min : Slippage(0.001, "0.1%")

    @Parcelize
    object Medium : Slippage(0.005, "0.5%")

    @Parcelize
    object One : Slippage(0.01, "1%")

    @Parcelize
    object TopUpSlippage : Slippage(0.03, "3%")

    @Parcelize
    object Five : Slippage(0.05, "5%")

    @Parcelize
    data class Custom(val value: Double) : Slippage(value, "${value.times(PERCENT_DIVIDE_VALUE)}")

    companion object {
        fun parse(slippage: Double): Slippage =
            when (slippage) {
                Min.doubleValue -> Min
                Medium.doubleValue -> Medium
                One.doubleValue -> One
                TopUpSlippage.doubleValue -> TopUpSlippage
                Five.doubleValue -> Five
                else -> if (slippage == 0.0) Min else Custom(slippage)
            }
    }
}
