package com.p2p.wallet.swap.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class Slippage(val doubleValue: Double, val percentValue: String) : Parcelable {
    @Parcelize
    object MIN : Slippage(0.001, "0.1%")

    @Parcelize
    object MEDIUM : Slippage(0.005, "0.5%")

    @Parcelize
    object PERCENT : Slippage(0.01, "1%")

    @Parcelize
    object FIVE : Slippage(0.05, "5%")

    @Parcelize
    data class CUSTOM(val value: Double) : Slippage(value, "$value")

    companion object {
        fun parse(slippage: Double): Slippage =
            when (slippage) {
                MIN.doubleValue -> MIN
                MEDIUM.doubleValue -> MEDIUM
                PERCENT.doubleValue -> PERCENT
                FIVE.doubleValue -> FIVE
                else -> CUSTOM(slippage)
            }
    }
}