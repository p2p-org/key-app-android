package org.p2p.wallet.moonpay.model

import java.math.BigDecimal

sealed class MoonpayBuyResult {
    data class Success(val data: BuyCurrency) : MoonpayBuyResult()
    data class Error(val message: String) : MoonpayBuyResult()
    data class MinAmountError(val minBuyAmount: BigDecimal) : MoonpayBuyResult()
    data class MaxAmountError(val maxBuyAmount: BigDecimal) : MoonpayBuyResult()
}
