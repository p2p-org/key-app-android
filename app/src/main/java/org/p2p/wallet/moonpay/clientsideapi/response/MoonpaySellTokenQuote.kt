package org.p2p.wallet.moonpay.clientsideapi.response

import java.math.BigDecimal

class MoonpaySellTokenQuote(
    val tokenDetails: MoonpayCurrency.CryptoToken,
    val tokenAmount: BigDecimal,
    val tokenPrice: BigDecimal,
    val fiatDetails: MoonpayCurrency.Fiat,
    val paymentMethod: MoonpaySellPaymentMethod,
    val extraFeeAmount: Int,
    val feeAmountInToken: BigDecimal,
    val feeAmountInFiat: BigDecimal,
    val fiatEarning: BigDecimal,
)
