package com.p2p.wallet.swap.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class Slippage(val doubleValue: Double) : Parcelable {
    @Parcelize object MIN : Slippage(0.1)
    @Parcelize object MEDIUM : Slippage(0.5)
    @Parcelize object PERCENT : Slippage(1.0)
    @Parcelize object FIVE : Slippage(5.0)
    @Parcelize data class CUSTOM(val value: Double) : Slippage(value)

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