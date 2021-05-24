package com.p2p.wallet.swap.model

enum class Slippage(val doubleValue: Double) {
    MIN(0.1),
    MEDIUM(0.5),
    PERCENT(1.0),
    FIVE(5.0),
    CUSTOM(0.0);

    companion object {
        fun parse(slippage: Double): Slippage =
            when (slippage) {
                MIN.doubleValue -> MIN
                MEDIUM.doubleValue -> MEDIUM
                PERCENT.doubleValue -> PERCENT
                FIVE.doubleValue -> FIVE
                else -> CUSTOM
            }
    }
}