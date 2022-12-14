package org.p2p.wallet.moonpay.clientsideapi.response

class MoonpaySellTokenQuote(
    val tokenDetails: MoonpayCurrency.CryptoToken,
    val tokenAmount: Double,
    val tokenPrice: Double,
    val fiatDetails: MoonpayCurrency.Fiat,
    val paymentMethod: MoonpaySellPaymentMethod,
    val extraFeeAmount: Int,
    val feeAmount: Double,
    val fiatEarning: Double,
)
