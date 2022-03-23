package org.p2p.wallet.moonpay.model

sealed class MoonpayBuyResult {
    data class Success(val data: BuyCurrency) : MoonpayBuyResult()
    data class Error(val message: String) : MoonpayBuyResult()
}
