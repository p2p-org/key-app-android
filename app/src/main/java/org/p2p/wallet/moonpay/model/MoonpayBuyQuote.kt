package org.p2p.wallet.moonpay.model

import org.p2p.wallet.home.model.Token
import java.math.BigDecimal

data class MoonpayBuyQuote(
    val currency: String,
    val token: Token,
    val price: BigDecimal,
    val minAmount: BigDecimal
)
