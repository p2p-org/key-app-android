package org.p2p.wallet.moonpay.model

import java.math.BigDecimal
import org.p2p.core.token.Token
import org.p2p.wallet.moonpay.repository.sell.FiatCurrency

data class MoonpayBuyQuote(
    val currency: FiatCurrency,
    val token: Token,
    val price: BigDecimal,
    val minAmount: BigDecimal
)
