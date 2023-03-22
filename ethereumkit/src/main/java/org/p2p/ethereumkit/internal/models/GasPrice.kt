package org.p2p.ethereumkit.internal.models

sealed class GasPrice {
    class Legacy(val legacyGasPrice: Long) : GasPrice() {
        override val value: Long
            get() = legacyGasPrice
    }
    class Eip1559(val maxFeePerGas: Long, val maxPriorityFeePerGas: Long) : GasPrice() {
        override val value: Long
            get() = maxFeePerGas
    }

    abstract val value: Long

    val max: Long
        get() = when (this) {
            is Eip1559 -> maxFeePerGas
            is Legacy -> legacyGasPrice
        }

    override fun toString() = when (this) {
        is Eip1559 -> "Eip1559 [maxFeePerGas: $maxFeePerGas, maxPriorityFeePerGas: $maxPriorityFeePerGas]"
        is Legacy -> "Legacy [gasPrice: $legacyGasPrice]"
    }
}
