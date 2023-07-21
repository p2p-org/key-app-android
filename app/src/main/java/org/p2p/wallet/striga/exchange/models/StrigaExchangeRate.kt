package org.p2p.wallet.striga.exchange.models

import java.math.BigDecimal
import org.p2p.core.utils.MillisSinceEpoch

data class StrigaExchangeRate(
    val priceUsd: BigDecimal,
    val buyRate: BigDecimal,
    val sellRate: BigDecimal,
    val timestamp: MillisSinceEpoch,
    val currencyName: String,
)
