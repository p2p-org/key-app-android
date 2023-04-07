package org.p2p.wallet.moonpay.model

import java.math.BigDecimal

sealed interface MoonpayBuyResult {
    data class Success(val data: BuyCurrency) : MoonpayBuyResult
    data class Error(
        override val message: String,
        override val cause: Throwable? = null
    ) : MoonpayBuyResult, Throwable()
    data class MinAmountError(val minBuyAmount: BigDecimal) : MoonpayBuyResult
    data class MaxAmountError(val maxBuyAmount: BigDecimal) : MoonpayBuyResult
}
