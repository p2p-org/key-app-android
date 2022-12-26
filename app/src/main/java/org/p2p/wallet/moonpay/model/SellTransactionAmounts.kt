package org.p2p.wallet.moonpay.model

import org.p2p.wallet.moonpay.repository.sell.MoonpaySellFiatCurrency
import java.math.BigDecimal

class SellTransactionAmounts(
    val tokenAmount: BigDecimal,
    val feeAmount: BigDecimal,
    val usdAmount: BigDecimal,
    val eurAmount: BigDecimal,
    val gbpAmount: BigDecimal
) {
    fun getAmountFromFiat(fiat: MoonpaySellFiatCurrency): BigDecimal = when (fiat) {
        MoonpaySellFiatCurrency.EUR -> eurAmount
        MoonpaySellFiatCurrency.USD -> usdAmount
        MoonpaySellFiatCurrency.GBP -> gbpAmount
    }
}
